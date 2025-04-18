import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Tablazat extends Application {
    public static final int SIZE = 100;
    public static final int WIDTH = 5;
    public static final int HEIGHT = 8;
    public static List<List<Rect>> rectList;
    public static int level;
    private Move move;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tetris");
        rectList = new ArrayList<>();
        for (int i = 0; i < HEIGHT; i++) {
            rectList.add(new ArrayList<>());
        }
        level = 0;

        Group root = new Group();
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(WIDTH * SIZE, HEIGHT * SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        draw(gc);

        move = new Move(rectList, level, gc);

        root.getChildren().add(canvas);
        Scene scene = new Scene(root, WIDTH * SIZE, HEIGHT * SIZE); // Beállítjuk a jelenet méretét
        primaryStage.setScene(scene); // Beállítjuk a jelenetet az ablakhoz
        primaryStage.show();

        scene.setOnKeyPressed(event -> handleKeyPress(event));

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> move.force()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }



    public void handleKeyPress(javafx.scene.input.KeyEvent event) {
        switch (event.getCode()) {
            case A:
                move.moveLeft();
                break;
            case D:
                move.moveRight();
                break;
            default:
                break;
        }
    }


    public void draw(GraphicsContext gc) {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                Rect rect = new Rect(gc, i, j, SIZE, Color.WHITE);
                rect.setRect();
                rectList.get(i).add(rect);
            }
        }
    }


    public List<Rect> getRectListIndex(int index) {
        return rectList.get(index);
    }
}