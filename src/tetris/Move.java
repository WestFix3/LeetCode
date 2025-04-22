package tetris;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Move {
    public List<List<Rects>> rectList;
    private int level;
    private int currentX;
    private int currentY;
    private GraphicsContext gc;
    private boolean over = false;
    private boolean clean = false;
    private int rowToClean = -1;
    private Shape shape;

    public Move(List<List<Rects>> rectList, int level, GraphicsContext gc) {
        this.rectList = rectList;
        this.level = level;
        this.gc = gc;
        this.currentX = 2;
        this.currentY = 0;
    }

    public void moveLeft() {
        rectList.get(currentY).get(currentX).setColor(Color.WHITE);
        if (currentX > 0) {
            currentX--;
            currentY--;
            rectList.get(currentY).get(currentX).setColor(Color.RED);
            force();
        }
    }

    public void moveRight() {
        rectList.get(currentY).get(currentX).setColor(Color.WHITE);
        if (currentX < Table.WIDTH - 1) {
            currentX++;
            currentY--;
            rectList.get(currentY).get(currentX).setColor(Color.RED);
            force();
        }
    }

    public void force() {
        if(currentY == 0){
            shape = new I(rectList);
        }

        if (clean) {
            for(int i=0; i<Table.HEIGHT; i++){
                clearRow(i);
            }
            clean = false;
            rowToClean = -1;
        }

        for (int i = 0; i < Table.HEIGHT; i++) {
            for (int j = 0; j < Table.WIDTH; j++) {
                Rects rect = rectList.get(i).get(j);
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
            rectList.get(currentY-1).get(currentX).setHely(true);
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
        for (Rects rect : rectList.get(y)) {
            if (!rect.getColor().equals(firstColor)) {
                return false;
            }
        }
        return true;
    }

    private void clearRow(int y) {
        if(allOneColor(y)) {
            for (Rects rect : rectList.get(y)) {
                rect.setColor(Color.WHITE);
                rect.setRect();
                rect.setHely(false);
            }
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

    public void kiirLista(){
        for(int i=0; i< Table.HEIGHT; i++){
            for(int j=0; j<Table.WIDTH; j++){
                System.out.print(rectList.get(i).get(j).getHely() + " ");
            }
            System.out.println();
        }
        System.out.println("ASDASDASDASDASDASDASDASDASDASDSADSADAS");
    }
}
