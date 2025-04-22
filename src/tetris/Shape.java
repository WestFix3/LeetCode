package tetris;

import java.util.List;

public abstract class Shape {
    public List<List<Rects>> rectList;
    private List<Shape> shapes;// = List.of(new I(rectList), new T(rectList));

    public Shape(List<List<Rects>> rectList){
        this.rectList = rectList;
    }

    public abstract void MoveDown(int x, int y);

    public abstract void MoveTo(int x, int y);

    public abstract void Delete();

    protected boolean isPositive(int x){
        if(x >= 0 && x < 8){
            return true;
        }

        return false;
    }
}
