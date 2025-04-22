package tetris;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Rects {
    private GraphicsContext gc;
    private int i;
    private int j;
    private int SIZE;
    private Color color;
    private boolean helyen_van;

    public Rects(GraphicsContext gc, int i, int j, int SIZE, Color color){
        this.gc = gc;
        this.i = i;
        this.j = j;
        this.SIZE = SIZE;
        this.color = color;
        this.helyen_van = false;
    }

    public void setRect(){
        gc.setFill(color);
        gc.fillRect(j * SIZE, i*SIZE, SIZE, SIZE);
    }

    public Color getColor(){
        return color;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public boolean getHely(){
        return helyen_van;
    }

    public void setHely(boolean hely){
        helyen_van = hely;
    }
}
