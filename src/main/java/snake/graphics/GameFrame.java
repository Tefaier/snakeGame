package snake.graphics;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
  public GameFrame() throws HeadlessException {
    setBounds(200, 200, 408, 430);
    add(new GameField(new Rectangle(400, 400), 15, 15, 2));
    setVisible(true);
  }
}
