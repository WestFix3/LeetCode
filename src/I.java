import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class I extends Shape {
    private int[] x;
    private int[] y;
    private List<List<Rect>> rectList;
    public I(List<List<Rect>> rectList){
        super(rectList);
        this.rectList = rectList;
        x = new int[]{0,-1};
        y = new int[]{2,2};
    }

    @Override
    public void MoveDown(int newX, int newY){
        for(int i=0; i<x.length; i++){
            if(isPositive(x[i]-1) && isPositive(y[i]-1)) {
                rectList.get(x[i]).get(y[i]).setColor(Color.WHITE);
                rectList.get(x[i]).get(y[i]).setRect();
            }

            if(isPositive(x[i]) && isPositive(y[i])) {
                rectList.get(x[i]).get(y[i]).setColor(Color.RED);
                rectList.get(x[i]).get(y[i]).setRect();
            }
            x[i]++;
        }
    }

    @Override
    public void MoveTo(int x, int y){

    }

    @Override
    public void Delete(){
        for(int i=0; i<x.length; i++){
            Rect rect = rectList.get(x[i]).get(y[i]);
            rect.setColor(Color.WHITE);
            rect.setRect();
        }
    }
}
