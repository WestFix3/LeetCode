import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Move {
    private List<List<Rect>> rectList;
    private int level;
    private int currentX;
    private int currentY;
    private GraphicsContext gc;

    public Move(List<List<Rect>> rectList, int level, GraphicsContext gc) {
        this.rectList = rectList;
        this.level = level;
        this.gc = gc;
        this.currentX = 2;
        this.currentY = 0;
    }

    public void moveLeft() {
        if (currentX > 0) {
            currentX--;
            force();
        }
    }

    public void moveRight() {
        if (currentX < Tablazat.WIDTH - 1) {
            currentX++;
            force();
        }
    }

    public void force() {
        for (int i = 0; i < Tablazat.HEIGHT; i++) {
            for (int j = 0; j < Tablazat.WIDTH; j++) {
                Rect rect = rectList.get(i).get(j);
                if (i == currentY && j == currentX) {
                    rect.setColor(Color.RED);
                } else if (!rect.getHely()) {
                    rect.setColor(Color.WHITE);
                }
                rect.setRect();
            }
        }

        currentY++;

        if (collideOrEnd(currentY, currentX)) {
            rectList.get(currentY).get(currentX).setHely(true);
            currentY = 0;
        }
    }

    private boolean collideOrEnd(int x, int y) {
        if (x < Tablazat.HEIGHT - 1) {
            if (rectList.get(x + 1).get(y).getColor() == Color.RED) {
                return true;
            }
        } else {
            return true;
        }

        return false;
    }
}
