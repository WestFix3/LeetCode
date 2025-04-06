import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Random;

public class BattleGame extends Application {
    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);

        Random rand = new Random();
        Apple apple = new Apple(rand.nextInt(800), rand.nextInt(600));
        root.getChildren().add(apple.getApple_segments());

        Character player1 = new Character(100, 300, apple);
        player1.getSegments().forEach(root.getChildren()::add);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.A) {
                player1.move(-10, 0);
            } else if (event.getCode() == KeyCode.D) {
                player1.move(10, 0);
            } else if (event.getCode() == KeyCode.W) {
                player1.move(0, -10);
            } else if (event.getCode() == KeyCode.S) {
                player1.move(0, 10);
            } else if (event.getCode() == KeyCode.SPACE) {
                player1.takeApple();
                root.getChildren().add(player1.getSegments().get(player1.getSegments().size() - 1));
                apple.setXandY(rand.nextInt(800), rand.nextInt(600));
            }
        });

        primaryStage.setTitle("Battle Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
