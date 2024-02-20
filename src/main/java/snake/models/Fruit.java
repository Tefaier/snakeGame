package snake.models;

import snake.graphics.GameField;
import snake.graphics.GameField.FieldPosition;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Fruit {

  private GameField gameField;
  private FieldPosition position;
  private int size;

  public Fruit(GameField gameField, int spawnDelay, int size) {
    this.gameField = gameField;
    this.size = size;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(() -> {
      try {
        Thread.sleep(spawnDelay);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      spawn();
    });
  }

  public void draw(Graphics g) {
    if (position == null) {
      return;
    }
    var framePos = gameField.getFramePosition(position);
    float f = 0.8f;
    g.fillOval(framePos.x() + (int) (size * (1 - f) / 2), framePos.y() + (int) (size * (1 - f) / 2), (int) (size * f), (int) (size * f));
  }

  public void getEaten() {
    spawn();
  }

  private void spawn() {
    position = gameField.getRandomEmptyFieldPos();
    gameField.setFieldType(position, GameField.FieldType.Fruit);
  }
}
