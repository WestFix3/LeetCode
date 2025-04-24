package tetris;

import javafx.scene.paint.Color;

import java.util.List;

public class Z extends Shape {
    private int[] x;
    private int[] y;
    public List<List<Rects>> rectList;

    public Z(List<List<Rects>> rectList){
        super(rectList);
        this.rectList = rectList;
        setRectCount(2);
        x = new int[]{0,-1};
        y = new int[]{2,2};
    }

    @Override
    public boolean MoveDown(){
        for(int i=0; i<x.length; i++){
            if(isPositive(x[i]-1) && isPositive(y[i])) {
                rectList.get(x[i]-1).get(y[i]).setColor(Color.WHITE);
                rectList.get(x[i]-1).get(y[i]).setRect();
            }

            if(isPositive(x[i]) && isPositive(y[i])) {
                rectList.get(x[i]).get(y[i]).setColor(Color.RED);
                rectList.get(x[i]).get(y[i]).setRect();
            }
            x[i]++;
        }

        return false;
    }

    @Override
    public void moveTo(boolean irany){
        for(int i=0; i<y.length; i++){
            if(irany){
                y[i] -= 1;
            }else{
                y[i] += 1;
            }
        }
    }

    @Override
    public int[] getPos(int pos){
        int[] ret = new int[2];
        ret[0] = x[pos];
        ret[1] = y[pos];

        return ret;
    }
}
