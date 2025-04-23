package tetris;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Move {
    public List<List<Rects>> rectList;
    private int level;
    private GraphicsContext gc;
    private boolean over = false;
    private boolean clean = false;
    private int curr;
    private Shape shape;

    public Move(List<List<Rects>> rectList, int level, GraphicsContext gc) {
        this.rectList = rectList;
        this.level = level;
        this.gc = gc;
        this.curr = 0;
    }

    public void moveLeft() {
        for(int i=0; i<shape.getRectCount(); i++){
            int[] pos = shape.getPos(i);
            rectList.get(pos[1]).get(pos[0]).setColor(Color.WHITE);
            if (pos[0] > 0) {
                shape.moveTo(true);
                rectList.get(pos[1]).get(pos[0]).setColor(Color.RED);
                force();
            }
        }
    }

    public void moveRight() {
        for(int i=0; i<shape.getRectCount(); i++) {
            int[] pos = shape.getPos(i);
            rectList.get(pos[1]).get(pos[0]).setColor(Color.WHITE);
            if (pos[0] < Table.WIDTH - 1) {
                shape.moveTo(false);
                rectList.get(pos[1]).get(pos[0]).setColor(Color.RED);
                force();
            }
        }
    }

    public void force() {
        if(curr == 0){
            shape = new I(rectList);
        }

        if (clean) {
            for(int i=0; i<Table.HEIGHT; i++){
                clearRow(i);
            }
            clean = false;
        }

        for(int i=0; i<shape.getRectCount(); i++){
            int[] pos = shape.getPos(i);
            if(shape.isPositive(pos[0]) && shape.isPositive(pos[1])){
                shape.MoveDown(pos[0], pos[1]);
            }
        }

        if (over) {
            Over();
        }

        Collide();
    }

    public void Collide(){
        for(int i=0; i<shape.getRectCount(); i++){
            int[] pos = shape.getPos(i);
            if (collideOrEnd(pos[0], pos[1])) {
                if (allOneColor(pos[1])) {
                    clean = true;
                }
                //Z type
                //rectList.get(pos[1]-1).get(pos[0]).setHely(true);
                //rectList.get(pos[1]).get(pos[0]-1).setHely(true);

                //I type
                rectList.get(pos[1]).get(pos[0]).setHely(true);
                rectList.get(pos[1]-1).get(pos[0]).setHely(true);
            } else {
                curr++;
            }
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