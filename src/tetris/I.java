package tetris;

import javafx.scene.paint.Color;

import java.util.List;

public class I extends Shape {
    private int[] x;
    private int[] y;
    public List<List<Rects>> rectList;
    public I(List<List<Rects>> rectList){
        super(rectList);
        this.rectList = rectList;
        setRectCount(2);
        x = new int[]{0,-1};
        y = new int[]{2,2};
    }

    @Override
    public boolean MoveDown(){
        for(int i=0; i<x.length; i++){
            if(isPositive(x[i]) && isPositive(y[i])){
                if(rectList.get(x[i]).get(y[i]).getHely()){
                    //!!!System.out.println("Collision detected at: " + x[i] + ", " + y[i]);
                    return true;
                }
                rectList.get(x[i]).get(y[i]).setColor(Color.WHITE);
                rectList.get(x[i]).get(y[i]).setRect();
            }
        }

        for(int i=0; i<x.length; i++){
            x[i]++;
        }

        for(int i=0; i<x.length; i++){
            if(isPositive(x[i]) && isPositive(y[i])) {
                rectList.get(x[i]).get(y[i]).setColor(Color.RED);
                rectList.get(x[i]).get(y[i]).setRect();
                //!!!System.out.println((i+1) + ". Moved to: " + x[i] + ", " + y[i]);
            }
        }

        return false;
    }


    @Override
    public void moveTo(boolean irany){
        for(int i=0; i<y.length; i++){
            rectList.get(x[i]).get(y[i]).setColor(Color.WHITE);
            if(irany){
                if(x[i] > 0){
                    y[i] -= 1;
                    x[i] -= 1;
                    rectList.get(x[i]).get(y[i]).setColor(Color.RED);
                }
            }else{
                if(x[i] < Table.WIDTH - 1){
                    y[i] += 1;
                    x[i] -= 1;
                    rectList.get(x[i]).get(y[i]).setColor(Color.RED);
                }
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
