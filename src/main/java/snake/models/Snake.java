package snake.models;

import snake.graphics.GameField;
import snake.graphics.GameField.FieldPosition;
import snake.graphics.GameField.FieldType;
import snake.graphics.GameField.FramePosition;

import java.awt.*;
import java.util.ArrayDeque;

public class Snake {
  public static enum Directions {
    UP,
    RIGHT,
    DOWN,
    LEFT,
    STAY
  }

  private GameField gameField;
  private int size = 10;
  private Directions direction = Directions.STAY;
  private Directions prevMove = Directions.STAY;
  // shows parts of snake 0-beginning, last-head
  private ArrayDeque<FieldPosition> body = new ArrayDeque<>();

  private boolean isAlive = true;
  private int deathDuration;
  private long deathTime;

  public Snake() {}

  public Snake(GameField gameField, int cordX, int cordY, int size) {
    this.gameField = gameField;
    body.add(new FieldPosition(cordX, cordY));
    gameField.setFieldType(body.getLast(), FieldType.Snake);
    this.size = size;
  }

  public boolean isAlive() {
    return isAlive;
  }

  public void draw(Graphics g) {
    int counter = body.size();
    // from 0 to 1
    float state = isAlive ? 0 : (float) (System.currentTimeMillis() - deathTime) / deathDuration;
    for (FieldPosition part : body) {
      FramePosition framePosition = gameField.getFramePosition(part);
      if (!isAlive) {
        float partState = 1 + counter * 0.25f - state * (1 + body.size() * 0.25f);
        partState = partState < 0 ? 0 : (partState > 1 ? 1 : partState);
        int partSize = (int) (size * partState);
        if (partSize != 0) {
          g.fillRect(
              framePosition.x() + (size - partSize) / 2,
              framePosition.y() + (size - partSize) / 2,
              partSize,
              partSize);
        }
      } else {
        g.fillRect(framePosition.x(), framePosition.y(), size, size);
      }
      --counter;
    }
  }

  public Snake move() {
    if (!isAlive || direction == Directions.STAY) {
      return this;
    }
    prevMove = direction;
    FieldPosition newPosition = offsetByDirection(body.getLast(), direction);
    FieldType fieldType = gameField.getFieldType(newPosition);
    switch (fieldType) {
      case Empty -> {
        gameField.setFieldType(body.removeFirst(), FieldType.Empty);
        body.add(newPosition);
        gameField.setFieldType(body.getLast(), FieldType.Snake);
      }
      case Fruit -> {
        body.add(newPosition);
        gameField.setFieldType(body.getLast(), FieldType.Snake);
        gameField.eatFruit();
      }
      case Border -> die();
      case Snake -> {
        if (!newPosition.equals(body.getFirst())) {
          die();
        } else {
          // it's allowed to go in <closed> cycle
          body.removeFirst();
          body.add(newPosition);
        }
      }
    }

    return this;
  }

  public void changeDirection(Directions direction) {
    switch (direction) {
      case UP -> this.direction = this.prevMove == Directions.DOWN ? this.direction : direction;
      case DOWN -> this.direction = this.prevMove == Directions.UP ? this.direction : direction;
      case RIGHT -> this.direction = this.prevMove == Directions.LEFT ? this.direction : direction;
      case LEFT -> this.direction = this.prevMove == Directions.RIGHT ? this.direction : direction;
    }
  }

  private FieldPosition offsetByDirection(FieldPosition fieldPosition, Directions direction) {
    FieldPosition toReturn;
    switch (direction) {
      case UP:
        toReturn = new FieldPosition(fieldPosition.x(), fieldPosition.y() - 1);
        break;
      case RIGHT:
        toReturn = new FieldPosition(fieldPosition.x() + 1, fieldPosition.y());
        break;
      case DOWN:
        toReturn = new FieldPosition(fieldPosition.x(), fieldPosition.y() + 1);
        break;
      case LEFT:
        toReturn = new FieldPosition(fieldPosition.x() - 1, fieldPosition.y());
        break;
      default:
        toReturn = null;
        break;
    }
    // if you don't want looped borders then comment line below
    toReturn = gameField.getLoopedPosition(toReturn);
    return toReturn;
  }

  private void die() {
    System.out.println("Game finished with score: " + body.size());
    isAlive = false;
    deathDuration = (int) Math.sqrt(body.size()) * 500;
    deathTime = System.currentTimeMillis();
    gameField.startDeath(body.size(), deathDuration);
  }
}
