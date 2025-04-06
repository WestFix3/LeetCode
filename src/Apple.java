import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Apple {
    private Circle target;
    public Apple(int x, int y){
        this.target = new Circle(x, y, 20);
        target.setFill(Color.RED);
    }

    public Circle getApple_segments(){
        return target;
    }

    public double getX(){
        return target.getCenterX();
    }

    public double getY(){
        return target.getCenterY();
    }

    public void setXandY(double x, double y){
        target.setCenterX(x);
        target.setCenterY(y);
    }
}
