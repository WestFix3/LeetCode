import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Move {
    private List<List<Rect>> rectList;
    private int level;
    private int currentX;
    private int currentY;
    private GraphicsContext gc;
    private boolean over = false;
    private boolean clean = false;
    private int rowToClean = -1;
    private Shape shape;

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
            currentY--;
            force();
        }
    }

    public void moveRight() {
        if (currentX < Table.WIDTH - 1) {
            currentX++;
            currentY--;
            force();
        }
    }

    public void force() {
        if(currentY == 0){
            shape = new I(rectList);
        }

        if (clean) {
            clearRow(rowToClean);
            clean = false;
            rowToClean = -1;
        }

        for (int i = 0; i < Table.HEIGHT; i++) {
            for (int j = 0; j < Table.WIDTH; j++) {
                Rect rect = rectList.get(i).get(j);
                if (i == currentY && j == currentX) {
                    shape.MoveDown(currentY, currentX);
                    //rect.setColor(Color.RED);
                } else if (!rect.getHely()) {
                    rect.setColor(Color.WHITE);
                    rect.setRect();
                }
            }
        }

        if (over) {
            Over();
        }

        if (collideOrEnd(currentX, currentY)) {
            if (allOneColor(currentY)) {
                clean = true;
                rowToClean = currentY;
            }
            rectList.get(currentY).get(currentX).setHely(true);
            currentX = 2;
            currentY = 0;
        } else {
            currentY++;
        }
    }

    private boolean collideOrEnd(int x, int y) {
        if (y < Table.HEIGHT - 1) {
            if (rectList.get(y + 1).get(x).getColor() == Color.RED) {
                if (isItOver(x, y)) {
                    over = true;
                }
                return true;
            }
        } else {
            return true;
        }

        return false;
    }

    private boolean allOneColor(int y) {
        Color firstColor = rectList.get(y).get(0).getColor();
        for (Rect rect : rectList.get(y)) {
            if (!rect.getColor().equals(firstColor)) {
                return false;
            }
        }
        return true;
    }

    private void clearRow(int y) {
        for (Rect rect : rectList.get(y)) {
            rect.setColor(Color.WHITE);
            rect.setRect();
            rect.setHely(false);
        }
    }

    private boolean isItOver(int x, int y) {
        if (y == 0) {
            return true;
        }
        return false;
    }

    private void Over() {
        System.out.println("GAME OVER");
        System.exit(1);
    }
}
