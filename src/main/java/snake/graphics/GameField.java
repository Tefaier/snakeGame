package snake.graphics;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import snake.models.Fruit;
import snake.models.Snake;
import snake.models.Text;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;
import javax.swing.*;

public class GameField extends JPanel implements KeyListener, ActionListener {
  // shows position in relation to the GameField (0->...)
  public record FieldPosition(int x, int y) {}
  // shows position in relation to the Frame
  public record FramePosition(int x, int y) {}

  public enum FieldType {
    Empty,
    Snake,
    Fruit,
    Border
  }

  private final Logger logger = Logger.getLogger(GameField.class.getSimpleName());

  private Timer timer;
  private Snake snake;
  private Fruit fruit;
  private Text textField;

  // first index x (->) then y (UP), both start from 0
  private FieldType[][] fieldBlockState;
  private final Rectangle frameSize;
  private final int xDimension;
  private final int yDimension;
  private final int separation;

  private boolean awaitStart = true;

  public GameField(Rectangle frameSize, int xDimension, int yDimension, int separation) {
    this.frameSize = frameSize;
    this.xDimension = xDimension;
    this.yDimension = yDimension;
    this.separation = separation;

    fieldBlockState = new FieldType[xDimension][];
    for (int i = 0; i < xDimension; i++) {
      fieldBlockState[i] = new FieldType[yDimension];
      Arrays.fill(fieldBlockState[i], FieldType.Empty);
    }

    setFont(new Font("Courier", Font.BOLD,20));
    textField = new Text(frameSize.width / 2, frameSize.height / 2,"Press any button to start", true);
    setFocusable(true);
    addKeyListener(this);

    timer = new Timer(200, this);
    timer.start();
  }

  private void reset() {
    awaitStart = false;
    for (int i = 0; i < xDimension; i++) {
      Arrays.fill(fieldBlockState[i], FieldType.Empty);
    }
    snake = new Snake(this , xDimension / 2, yDimension / 2, getBlockSize());
    fruit = new Fruit(this, 3000, getBlockSize());
    textField.setTextField(null, false);
    timer.setDelay(200);
  }

  // uses field x data
  public int getBlockSize() {
    return (frameSize.width - separation * (xDimension + 1)) / xDimension;
  }

  // returns left-upper corner
  public FramePosition getFramePosition(FieldPosition fieldPosition) {
    int size = getBlockSize();
    return new FramePosition(
        (int) ((fieldPosition.x + 1) * separation + (fieldPosition.x) * size),
        (int) ((fieldPosition.y + 1) * separation + (fieldPosition.y) * size)
    );
  }

  public FieldType getFieldType(FieldPosition fieldPosition) {
    try {
      return fieldBlockState[fieldPosition.x][fieldPosition.y];
    } catch (IndexOutOfBoundsException e) {
      // lazy realisation of border check
      return FieldType.Border;
    }
  }

  public void setFieldType (FieldPosition fieldPosition, FieldType fieldType) {
    try {
      fieldBlockState[fieldPosition.x][fieldPosition.y] = fieldType;
    } catch (IndexOutOfBoundsException e) {
      logger.warning(
          "Field type was tried to be set out of field bounces with type: " +
              fieldType.name() +
              " and position: " +
              fieldPosition.toString()
      );
    }
  }

  public FieldPosition getRandomEmptyFieldPos() {
    // random pick
    for (int i = 0; i < 10; ++i) {
      int x = (int)(Math.random() * xDimension);
      int y = (int)(Math.random() * yDimension);
      if (fieldBlockState[x][y] == FieldType.Empty) {
        return new FieldPosition(x, y);
      }
    }

    // full search
    long emptyNum = Arrays.stream(fieldBlockState).flatMap(x -> Arrays.stream(x)).filter(x -> x == FieldType.Empty).count();
    if (emptyNum == 0) {
      return null;
    }
    int picked = (int)(Math.random() * emptyNum);
    int counter = -1;
    for (int x = 0; x < xDimension; ++x) {
      for (int y = 0; y < yDimension; ++y) {
        if (fieldBlockState[x][y] == FieldType.Empty) {
          ++counter;
          if (counter == picked) {
            return new FieldPosition(x, y);
          }
        }
      }
    }
    return null;
  }

  public FieldPosition getLoopedPosition(FieldPosition fieldPosition) {
    return new FieldPosition(Math.floorMod(fieldPosition.x(), xDimension), Math.floorMod(fieldPosition.y(), yDimension));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    textField.draw(g);
    if (!awaitStart) {
      snake.draw(g);
      fruit.draw(g);
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    if (awaitStart) {
      // starts the game
      reset();
      textField.setTextField(null, false);
      return;
    }
    switch (e.getKeyCode()) {
      case 37 -> snake.changeDirection(Snake.Directions.LEFT);
      case 38 -> snake.changeDirection(Snake.Directions.UP);
      case 39 -> snake.changeDirection(Snake.Directions.RIGHT);
      case 40 -> snake.changeDirection(Snake.Directions.DOWN);
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!awaitStart && snake.isAlive()) {
      snake.move();
    }
    repaint();
  }

  public void eatFruit() {
    fruit.getEaten();
  }

  public void startDeath(int score, int duration) {
    timer.setDelay(40);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(() -> {
      try {
        Thread.sleep(duration + 1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      textField.setTextField("YOU LOST\nScore: " + score + "\npress any button to restart", true);
      awaitStart = true;
    });
  }
}
