package com.example;

import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONObject;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.*;
import java.util.HashSet;
import java.util.Set;

public class Labyrinth extends JPanel {
  public static final int SIZE = 50; // גודל המבוך

  private BufferedImage image;
  private List<Point> solutionPath = new ArrayList<>();

  public Labyrinth() {
    this.setBounds(0, 0, MainFrame.WINDOW_WIDTH, MainFrame.WINDOW_HEIGTH);
    this.setLayout(null);
    this.image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB); // פיקסלים 100*100 ומתאים את התמונה לגודל
                                                                            // הפאנל
    int expectedPoints = SIZE * SIZE;
    paintPixels(this.image, expectedPoints);

    JButton button = new JButton("check solution");
    button.setBounds((MainFrame.WINDOW_WIDTH / 2) - 100, MainFrame.WINDOW_HEIGTH - 75, 200, 40);
    button.setFont(new Font("Monospaced", Font.PLAIN, 18));
    this.add(button);

    button.addActionListener((e) -> {
      if (checkSolution()) {
        System.out.println("solution exists");
      } else { // לשים בהערה כדי לבדוק את הלייבל
        System.out.println("solution doesn't exist");
        JLabel label = new JLabel("Solustion Doesn't Exist", SwingConstants.CENTER);
        label.setBounds(0, MainFrame.WINDOW_HEIGTH / 2 - MainFrame.WINDOW_HEIGTH
            / 10, MainFrame.WINDOW_WIDTH,
            MainFrame.WINDOW_HEIGTH / 10); // פרוס באמצע
        label.setFont(new Font("Monospaced", Font.PLAIN, 18));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(Color.RED);
        this.add(label);
      }
    });

    this.setVisible(true);

  }

  public void paintPixels(BufferedImage image, int expectedPoints) {
    // צביעת הפיקסלים השחורים
    for (int x = 0; x < SIZE; x++) {
      for (int y = 0; y < SIZE; y++) {
        image.setRGB(x, y, Color.BLACK.getRGB());
      }
    }
    // צביעת הפיקסלים הלבנים
    try {
      HttpResponse<String> response = Unirest.get("https://app.seker.live/fm1/get-points").queryString("width", SIZE)
          .queryString("height", SIZE).asString(); // המבוך בגודל שקבענו
      JSONArray pointsArray = new JSONArray(response.getBody());
      System.out.println(pointsArray.length());
      Set<String> pointsSeen = new HashSet<>(); // הנקודות שעברנו עליהם

      for (int i = 0; i < pointsArray.length(); i++) {
        JSONObject json = pointsArray.getJSONObject(i);
        // עבור כל נקודה לבנה
        int x = (int) json.getDouble("x");
        int y = (int) json.getDouble("y");
        String key = x + "," + y;

        if (!pointsSeen.contains(key)) {
          pointsSeen.add(key);
          if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) { // בדיקה שלא חרגנו מהתחום
            image.setRGB(x, y, Color.WHITE.getRGB());
          }
        }
      }
    } catch (UnirestException e) {
      throw new RuntimeException();
    }
    repaint();
  }

  public boolean checkSolution() {
    this.solutionPath.clear(); // מנקה את המסלול הקודם לפני הבדיקה הבאה

    // אם הפיקסל ההתחלתי או הסופי שחור אין פתרון
    if (image.getRGB(0, 0) == Color.BLACK.getRGB() ||
        image.getRGB(SIZE - 1, SIZE - 1) == Color.BLACK.getRGB()) {
      return false;
    }

    boolean[][] visited = new boolean[SIZE][SIZE]; // ברירת מחדל פולס
    boolean found = dfs(0, 0, visited); // תחילת החיפוש

    repaint();
    return found;
  }

  private boolean dfs(int x, int y, boolean[][] visited) { // בודקת עבור כל פיקסל אם אפשר להתקדם
    if (x == SIZE - 1 && y == SIZE - 1) { // תנאי עצירה
      this.solutionPath.add(new Point(x, y)); // הוספת נקודת הסיום למסלול
      return true;
    }

    visited[x][y] = true; // סימון פיקסל כבודק
    this.solutionPath.add(new Point(x, y));

    int[][] directions = {
        { 1, 0 }, // ימינה
        { -1, 0 }, // שמאלה
        { 0, 1 }, // למטה
        { 0, -1 } // למעלה
    };

    for (int[] dir : directions) { // בדיקה עבור כל כיוון אם אפשר להתקדם
      // מיקום אינדקסים
      int newX = x + dir[0];
      int newY = y + dir[1];

      if (isValid(newX, newY, visited)) {
        if (dfs(newX, newY, visited)) { // בדיקת הפיקסל הבא
          return true;
        }
      }
    }
    solutionPath.remove(solutionPath.size() - 1); // אם לא נמצא פתרון מהנקודה הזו נחזור אחורה
    return false;
  }

  private boolean isValid(int x, int y, boolean[][] visited) {
    return x >= 0 && x < SIZE &&
        y >= 0 && y < SIZE &&
        this.image.getRGB(x, y) != Color.BLACK.getRGB() &&
        !visited[x][y];
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (this.image != null) {
      int imageWidth = getWidth();
      int imageHeight = getHeight() - 50; // מקום לכפתור

      g.drawImage(this.image, 0, 0, imageWidth, imageHeight, null);

      // צביעת המסלול בירוק
      g.setColor(Color.GREEN);
      double pixelWidth = (double) imageWidth / SIZE;
      double pixelHeight = (double) imageHeight / SIZE;

      for (Point p : solutionPath) {
        if (image.getRGB(p.x, p.y) == Color.WHITE.getRGB()) {
          // ציור ריבוע קטן יותר בתוך הפיקסל כדי להשאיר שוליים ולא לחרוג
          int x = (int) (p.x * pixelWidth + pixelWidth * 0.2);
          int y = (int) (p.y * pixelHeight + pixelHeight * 0.2);
          int w = (int) (pixelWidth * 0.6);
          int h = (int) (pixelHeight * 0.6);

          g.fillRect(x, y, w, h);
        }
      }
    }
  }

}
