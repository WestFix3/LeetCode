import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SimpleButtonMessage extends Application {
    private Label winnerLabel = new Label("Nincs nyertes");

    @Override
    public void start(Stage primaryStage) {
        Buttons b1 = new Buttons(" ", 0, 0, this);
        Buttons b2 = new Buttons(" ", 0, 1, this);
        Buttons b3 = new Buttons(" ", 0, 2, this);
        Buttons b4 = new Buttons(" ", 1, 0, this);
        Buttons b5 = new Buttons(" ", 1, 1, this);
        Buttons b6 = new Buttons(" ", 1, 2, this);
        Buttons b7 = new Buttons(" ", 2, 0, this);
        Buttons b8 = new Buttons(" ", 2, 1, this);
        Buttons b9 = new Buttons(" ", 2, 2, this);

        b1.setOn();
        b2.setOn();
        b3.setOn();
        b4.setOn();
        b5.setOn();
        b6.setOn();
        b7.setOn();
        b8.setOn();
        b9.setOn();

        GridPane grid = new GridPane();
        grid.add(b1.getButton(), 0, 0);
        grid.add(b2.getButton(), 1, 0);
        grid.add(b3.getButton(), 2, 0);
        grid.add(b4.getButton(), 0, 1);
        grid.add(b5.getButton(), 1, 1);
        grid.add(b6.getButton(), 2, 1);
        grid.add(b7.getButton(), 0, 2);
        grid.add(b8.getButton(), 1, 2);
        grid.add(b9.getButton(), 2, 2);

        VBox root = new VBox();
        root.getChildren().addAll(grid, winnerLabel);

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("TicTacToe");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void setWinner(String winner) {
        winnerLabel.setText(winner + " nyert!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

