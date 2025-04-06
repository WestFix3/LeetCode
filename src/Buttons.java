import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

class Buttons {
    public static int counter = 0;
    static List<List<Integer>> lista = new ArrayList<>();
    Button button;
    int x;
    int y;
    SimpleButtonMessage app;

    public Buttons(String szoveg, int x, int y, SimpleButtonMessage app) {
        button = new Button(szoveg);
        this.x = x;
        this.y = y;
        this.app = app;
        if (lista.size() <= x) {
            lista.add(new ArrayList<>());
        }
        lista.get(x).add(y, -1); // -1 jelzi, hogy a mező üres
    }

    public void setOn() {
        button.setOnAction(e -> {
            counter++;
            button.setText(mezo(counter));
            lista.get(x).set(y, counter % 2); // 0 az "O", 1 az "X"
            if (isWinning()) {
                app.setWinner(mezo(counter));
            }
        });
    }

    public String mezo(int counter) {
        return (counter % 2 == 0) ? "O" : "X";
    }

    public Button getButton() {
        return button;
    }

    public boolean isWinning() {
        return checkRow(x) || checkColumn(y) || checkDiagonals();
    }

    private boolean checkRow(int row) {
        int first = lista.get(row).get(0);
        return first != -1 && lista.get(row).stream().allMatch(cell -> cell == first);
    }

    private boolean checkColumn(int col) {
        int first = lista.get(0).get(col);
        return first != -1 && IntStream.range(0, 3).allMatch(row -> lista.get(row).get(col) == first);
    }

    private boolean checkDiagonals() {
        int center = lista.get(1).get(1);
        if (center == -1) return false;
        return (lista.get(0).get(0) == center && lista.get(2).get(2) == center) ||
                (lista.get(0).get(2) == center && lista.get(2).get(0) == center);
    }
}
