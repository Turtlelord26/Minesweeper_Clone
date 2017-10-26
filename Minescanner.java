package reference;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * An independently coded Minesweeper-style game.
 * @author James Talbott
 */
public class Minescanner {
  
  /**
   * The difference between the total number of mines and the number of marks onscreen; used in the mine counter.
   */
  private int minesRemaining;
  
  private boolean leftMousePressed = false;
  
  private boolean rightMousePressed = false;
  
  private MinedJButton[] blocks;
  
  private BevelBorder unclicked = new BevelBorder(0);
  
  private BevelBorder clicked = new BevelBorder(1);
  
  public static void main(String[] args) {
    if (args.length == 0) {
      new Minescanner(16, 30, 99, true);
    }
    else if (args.length != 4)
      System.out.println("Command line: rows, columns, mines, color (color or black)");
    else {
      try {
        if (args[3].equals("color"))
          new Minescanner(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                          Integer.parseInt(args[2]), true);
        else if (args[3].equals("black"))
          new Minescanner(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                          Integer.parseInt(args[2]), false);
        else
          System.out.println("Error: required color argument is neither \"color\" or \"black\"");
      }
      catch (NumberFormatException e) {
        System.out.println("Error: a required numerical argument is not an integer");
      }
    }
  }
  
  public Minescanner(final int rows, final int columns, final int mines, final boolean color) {
    blocks = new MinedJButton[rows * columns];
    JFrame window = new JFrame("Minescanner");
    //Initializing the control bar
    JPanel controls = new JPanel(new GridLayout(1, 3));
    final JButton reset = new JButton("Reset");
    reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object o = ((JButton) e.getSource()).getTopLevelAncestor();
        if (o instanceof JFrame) {
          JFrame j = (JFrame) o;
          j.setVisible(false);
          String colorString;
          if (color)
            colorString = "color";
          else
            colorString = "black";
          String[] a = {new Integer(rows).toString(), new Integer(columns).toString(), 
            new Integer(mines).toString(), colorString};
          main(a);
        }
      }
    });
    controls.add(reset);
    minesRemaining = mines;
    final JLabel mineCounter = new JLabel("Mines: no boom :(", JLabel.CENTER);
    controls.add(mineCounter);
    //Initializing the board
    JPanel board = new JPanel(new GridLayout(rows, columns));
    MouseAdapter mousadap = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.getSource() instanceof MinedJButton && e.getButton() == MouseEvent.BUTTON3) {
          rightMousePressed = true;
          MinedJButton l = (MinedJButton) e.getSource();
          if (l.getBorder() == unclicked)
            mark(l, mineCounter);
        }
        else if (e.getSource() instanceof MinedJButton && e.getButton() == MouseEvent.BUTTON1)
          leftMousePressed = true;
      }
      public void mouseReleased(MouseEvent e) {
        Object o = e.getSource();
        if (o instanceof MinedJButton && ((e.getButton() == MouseEvent.BUTTON1 && rightMousePressed) || 
                                     (e.getButton() == MouseEvent.BUTTON3 && leftMousePressed))) {
          MinedJButton l = (MinedJButton) o;
          if (l.getBorder() == clicked)
            stripMine(l, blocks, this, reset, rows, columns, mines, mineCounter);
        }
        if (o instanceof MinedJButton && e.getButton() == MouseEvent.BUTTON1) {
          leftMousePressed = false;
          MinedJButton l = (MinedJButton) o;
          if (l.getBorder() == unclicked && l.getBackground() != Color.orange && ! rightMousePressed)
            excavate(l, blocks, this, reset, rows, columns, mines, mineCounter);
        }
        else if (o instanceof MinedJButton && e.getButton() == MouseEvent.BUTTON3)
          rightMousePressed = false;
      }
    };
    for (int i = 0; i < rows * columns; i++) {
      blocks[i] = new MinedJButton(i);
      blocks[i].setBorder(unclicked);
      blocks[i].setBackground(Color.gray);
      blocks[i].addMouseListener(mousadap);
      blocks[i].setPreferredSize(new Dimension(20, 20));
      board.add(blocks[i]);
    }
    //Initializing mines and numbers
    initializeMines(blocks, mines);
    initializeNumbers(blocks, rows, columns);
    //Initializing the window
    window.add(board, "Center");
    window.add(controls, "North");
    window.pack();
    mineCounter.setText("Mines: " + minesRemaining);
    window.setVisible(true);
  }
  
  private void excavate(MinedJButton l, MinedJButton[] blocks, MouseAdapter mousadap,
                        JButton reset, int rows, int columns, int mines, JLabel mineCounter) {
    l.setBorder(clicked);
    if (l.getNumber() == -1) {
      l.setBackground(Color.red);
      loseGame(blocks, mousadap, reset, mineCounter);
    }
    else if (l.getNumber() == 0)
      excavateZero(l, blocks, mousadap, reset, rows, columns, mines, mineCounter);
    else
      l.setText(((Integer) l.getNumber()).toString());
    int winChecker = 0;
    for (int i = 0; i < blocks.length; i++)
      if (blocks[i].getBorder() == clicked)
        winChecker++;
    if (winChecker == rows * columns - mines)
      winGame(blocks, reset, mousadap, mineCounter);
  }
  
  private void excavateZero(MinedJButton l, MinedJButton[] blocks, MouseAdapter mousadap,
                            JButton reset, int rows, int columns, int mines, JLabel mineCounter) {
    int i = l.getPosition();
    boolean above = (int) (i / columns) != 0;
    boolean below = (int) (i / columns) != rows - 1;
    boolean left = i % columns != 0;
    boolean right = i % columns != columns - 1;
    if (above && blocks[i - columns].getBorder() == unclicked
          && blocks[i - columns].getBackground() != Color.orange) {
      excavate(blocks[i - columns], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
    if (below && blocks[i + columns].getBorder() == unclicked
          && blocks[i + columns].getBackground() != Color.orange) {
      excavate(blocks[i + columns], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
    if (left && blocks[i - 1].getBorder() == unclicked
          && blocks[i - 1].getBackground() != Color.orange) {
      excavate(blocks[i - 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
    if (right && blocks[i + 1].getBorder() == unclicked
          && blocks[i + 1].getBackground() != Color.orange) {
      excavate(blocks[i + 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
    if (above && left && blocks[i - columns - 1].getBorder() == unclicked
          && blocks[i - columns - 1].getBackground() != Color.orange) {
      excavate(blocks[i - columns - 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
    if (below && left && blocks[i + columns - 1].getBorder() == unclicked
          && blocks[i + columns - 1].getBackground() != Color.orange) {
      excavate(blocks[i + columns - 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
    if (above && right && blocks[i - columns + 1].getBorder() == unclicked
          && blocks[i - columns + 1].getBackground() != Color.orange) {
      excavate(blocks[i - columns + 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
    if (below && right && blocks[i + columns + 1].getBorder() == unclicked
          && blocks[i + columns + 1].getBackground() != Color.orange) {
      excavate(blocks[i + columns + 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
  }
  
  private void mark(MinedJButton l, JLabel mineCounter) {
    if (l.getBorder() == unclicked && l.getBackground() != Color.orange) {
      l.setBackground(Color.orange);
      minesRemaining--;
      mineCounter.setText("Mines: " + minesRemaining);
    }
    else if (l.getBorder() == unclicked && l.getBackground() == Color.orange) {
      l.setBackground(Color.gray);
      minesRemaining++;
      mineCounter.setText("Mines: " + minesRemaining);
    }
  }
  
  private void stripMine(MinedJButton l, MinedJButton[] blocks, MouseAdapter mousadap,
                         JButton reset, int rows, int columns, int mines, JLabel mineCounter) {
    int i = l.getPosition();
    boolean above = (int) (i / columns) != 0;
    boolean below = (int) (i / columns) != rows - 1;
    boolean left = i % columns != 0;
    boolean right = i % columns != columns - 1;
    int markCounter = 0;
    if (left && blocks[i - 1].getBackground() == Color.orange)
      markCounter++;
    if (right && blocks[i + 1].getBackground() == Color.orange)
      markCounter++;
    if (above && blocks[i - columns].getBackground() == Color.orange)
      markCounter++;
    if (below && blocks[i + columns].getBackground() == Color.orange)
      markCounter++;
    if (above && left && blocks[i - columns - 1].getBackground() == Color.orange)
      markCounter++;
    if (above && right && blocks[i - columns + 1].getBackground() == Color.orange)
      markCounter++;
    if (below && left && blocks[i + columns - 1].getBackground() == Color.orange)
      markCounter++;
    if (below && right && blocks[i + columns + 1].getBackground() == Color.orange)
      markCounter++;
    if (markCounter == l.getNumber()) {
      if (left && blocks[i - 1].getBackground() != Color.orange)
        excavate(blocks[i - 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
      if (right && blocks[i + 1].getBackground() != Color.orange)
        excavate(blocks[i + 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
      if (above && blocks[i - columns].getBackground() != Color.orange)
        excavate(blocks[i - columns], blocks, mousadap, reset, rows, columns, mines, mineCounter);
      if (below && blocks[i + columns].getBackground() != Color.orange)
        excavate(blocks[i + columns], blocks, mousadap, reset, rows, columns, mines, mineCounter);
      if (above && left && blocks[i - columns - 1].getBackground() != Color.orange)
        excavate(blocks[i - columns - 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
      if (above && right && blocks[i - columns + 1].getBackground() != Color.orange)
        excavate(blocks[i - columns + 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
      if (below && left && blocks[i + columns - 1].getBackground() != Color.orange)
        excavate(blocks[i + columns - 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
      if (below && right && blocks[i + columns + 1].getBackground() != Color.orange)
        excavate(blocks[i + columns + 1], blocks, mousadap, reset, rows, columns, mines, mineCounter);
    }
  }
  
  private void initializeMines(MinedJButton[] blocks, int mines) {
    int mineCounter = 0;;
    while (mineCounter < mines) {
      int location = (int) (Math.random() * blocks.length);
      if (blocks[location].hasMine() == false) {
        blocks[location].setHasMine(true);
        mineCounter++;
      }
    }
  }
  
  private void initializeNumbers(MinedJButton[] blocks, int rows, int columns) {
    for (int i = 0; i < blocks.length; i++) {
      boolean above = (int) (i / columns) != 0;
      boolean below = (int) (i / columns) != rows - 1;
      boolean left = i % columns != 0;
      boolean right = i % columns != columns - 1;
      if (blocks[i].hasMine() == false) {
        int mines = 0;
        if (left && blocks[i - 1].hasMine())
          mines++;
        if (right && blocks[i + 1].hasMine())
          mines++;
        if (above && blocks[i - columns].hasMine())
          mines++;
        if (below && blocks[i + columns].hasMine())
          mines++;
        if (above && left && blocks[i - columns - 1].hasMine())
          mines++;
        if (above && right && blocks[i - columns + 1].hasMine())
          mines++;
        if (below && left && blocks[i + columns - 1].hasMine())
          mines++;
        if (below && right && blocks[i + columns + 1].hasMine())
          mines++;
        blocks[i].setNumber(mines);
      }
    }
  }
  
  private void loseGame(MinedJButton[] blocks, MouseAdapter mousadap, JButton reset, JLabel mineCounter) {
    for (int i = 0; i < blocks.length; i++) {
      if (blocks[i].getNumber() == -1 && blocks[i].getBackground() != Color.orange) {
        blocks[i].setBackground(Color.red);
        blocks[i].setBorder(clicked);
      }
      else if (blocks[i].getBackground() == Color.orange && blocks[i].hasMine() == false) {
        blocks[i].setBackground(Color.yellow);
        blocks[i].setText("X");
        blocks[i].setBorder(clicked);
      }
      blocks[i].removeMouseListener(mousadap);
    }
    reset.setText("You Lose !  D:");
    mineCounter.setText("Mines: BOOM!");
  }
  
  private void winGame(MinedJButton[] blocks, JButton reset, MouseAdapter mousadap, JLabel mineCounter) {
    for (int i = 0; i < blocks.length; i++) {
      blocks[i].removeMouseListener(mousadap);
    }
    reset.setText(":D");
    mineCounter.setText("Mines: no boom :(");
  }
  
  private class MinedJButton extends JButton {
    
    private int position = -1;
    
    private int number = -1;
    
    private boolean hasMine = false;
    
    public MinedJButton(int position) {
      super();
      this.position = position;
    }
    
    public int getPosition() {
      return position;
    }
    
    public void setNumber(int number) {
      this.number = number;
    }
    
    public int getNumber() {
      return number;
    }
    
    public void setHasMine(boolean hasMine) {
      this.hasMine = hasMine;
    }
    
    public boolean hasMine() {
      return hasMine;
    }
  }
}