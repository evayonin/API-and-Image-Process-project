package com.example;

import javax.swing.JFrame;

public class MainFrame extends JFrame {
  private Labyrinth labyrinth;

  public static final int WINDOW_WIDTH = 600;
  public static final int WINDOW_HEIGTH = 600;

  public MainFrame(Labyrinth labyrinth) {
    this.labyrinth = labyrinth;
    this.setSize(WINDOW_WIDTH, WINDOW_HEIGTH);
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null);
    this.setResizable(false);
    this.add(this.labyrinth);
    this.setVisible(true);
  }
}
