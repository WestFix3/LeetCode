import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Character {
    private List<Rectangle> segments;
    private Apple apple;

    public Character(int x, int y, Apple apple) {
        this.segments = new ArrayList<>();
        Rectangle head = new Rectangle(x, y, 40, 40);
        head.setFill(Color.BLUE);
        this.segments.add(head);
        this.apple = apple;
    }

    public List<Rectangle> getSegments() {
        return segments;
    }

    public void takeApple() {
        if((segments.get(0).getX() - apple.getX()) < 5 && (segments.get(0).getY() - apple.getY()) < 5){
            Rectangle newSegment = new Rectangle(40, 40);
            newSegment.setFill(Color.BLUE);
            Rectangle lastSegment = segments.get(segments.size() - 1);
            newSegment.setX(lastSegment.getX());
            newSegment.setY(lastSegment.getY());
            segments.add(newSegment);
        }
    }

    public void move(double dx, double dy) {
        for (int i = segments.size() - 1; i > 0; i--) {
            Rectangle current = segments.get(i);
            Rectangle previous = segments.get(i - 1);
            current.setX(previous.getX());
            current.setY(previous.getY());
        }
        Rectangle head = segments.get(0);
        head.setX(head.getX() + dx);
        head.setY(head.getY() + dy);
    }
}
