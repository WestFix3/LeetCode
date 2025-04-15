import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;

public class Canvas extends Application {
    public static final int SIZE = 100;
    public static final int WIDTH = 5;
    public static final int HEIGHT = 8;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tetris");

        Group root = new Group();
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(WIDTH*SIZE, HEIGHT*SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        draw(gc);

        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public void draw(GraphicsContext gc) {
        Random rand = new Random();
        List<Color> colorList = List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW);
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                gc.setFill(colorList.get(rand.nextInt(colorList.size())));
                gc.fillRect(j * SIZE, i * SIZE, SIZE, SIZE);
            }
        }
    }
}
