package tetris;

import java.util.List;

public abstract class Shape {
    public List<List<Rects>> rectList;
    private int rectCount;
    private List<Shape> shapes;// = List.of(new I(rectList), new T(rectList));

    public Shape(List<List<Rects>> rectList){
        this.rectList = rectList;
    }

    public abstract void MoveDown(int x, int y);

    public abstract void moveTo(boolean irany);

    public abstract int[] getPos(int pos);

    protected boolean isPositive(int x){
        if(x >= 0 && x < 8){
            return true;
        }

        return false;
    }

    public int getRectCount(){
        return rectCount;
    }

    public void setRectCount(int rectCount){
        this.rectCount = rectCount;
    }
}
