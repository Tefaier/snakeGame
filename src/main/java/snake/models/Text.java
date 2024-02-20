package snake.models;

import java.awt.*;
import java.util.Arrays;

public class Text {
  private int x = 0;
  private int y = 0;
  private String text;
  private boolean isShown = false;

  public Text() {
  }

  public Text(int x, int y, String text, boolean isShown) {
    this.x = x;
    this.y = y;
    this.text = text;
    this.isShown = isShown;
  }

  public void draw (Graphics g) {
    if (!isShown) {
      return;
    }
    // some code used from https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
    FontMetrics metrics = g.getFontMetrics();
    String[] lines = text.split("\n");
    for (int i = 0; i < lines.length; i++) {
      int displayX = x - metrics.stringWidth(lines[i]) / 2;
      int displayY = y + metrics.getHeight() * (lines.length * -1) / 2;
      g.drawString(lines[i], displayX, displayY + i * metrics.getHeight());
    }

  }

  public void setTextField(String text, boolean isShown) {
    this.text = text;
    this.isShown = isShown;
  }
}
