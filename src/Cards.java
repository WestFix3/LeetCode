import javafx.scene.control.Button;
import java.util.*;

public class Cards {
    public static List<String> words = new ArrayList<>();
    public static List<Button> meglevok = new ArrayList<>();
    public static Map<String, Button> obj = new HashMap<>();
    public static Button new_game;
    Button button;
    String kerdes;
    String valasz;

    public Cards(String tartalom, String valasz){
        button = new Button(tartalom);
        button.setPrefSize(100, 50);
        this.kerdes = tartalom;
        this.valasz = valasz;
    }

    public Cards(String tartalom){
        new_game = new Button();
        new_game.setText(tartalom);
        new_game.setDisable(true);
    }

    public void setOn(){
        button.setOnAction(
                e -> {
                    if (meglevok.contains(button)) {
                        // Ha a gomb már megtalált pár, ne csináljon semmit
                        return;
                    }

                    if (words.size() < 2) {
                        if (button.getText().equals(kerdes) && !meglevok.contains(button)) {
                            button.setText(valasz);
                            words.add(valasz);
                            obj.put(valasz, getButton());
                        } else {
                            if (words.contains(valasz)) {
                                words.remove(valasz);
                                obj.remove(valasz);
                                button.setText(kerdes);
                            }
                        }
                    } else {
                        button.setText(kerdes);
                        if (words.contains(valasz)) {
                            words.remove(valasz);
                            obj.remove(valasz);
                        }
                    }

                    if (words.size() == 2) {
                        if (words.get(0).equals(words.get(1))) {
                            Button btn1 = obj.get(words.get(0));
                            Button btn2 = obj.get(words.get(1));
                            if (btn1 != null && btn2 != null) {
                                meglevok.add(btn1);
                                meglevok.add(btn2);
                            }
                            words.clear();
                            obj.clear();
                        }
                        if(meglevok.size() == 20){
                            new_game.setDisable(false);
                            new_game.setOnAction(
                                    f -> {
                                        generate();
                                    }
                            );
                        }
                    }
                }
        );
    }

    //Nem tökéletes
    public void generate(){
        List<String> szavak = List.of("alma", "banan", "cica", "kutya", "macska", "korte", "dinnye", "eper", "lo", "hal");
        int[] mennyiseg = new int[szavak.size()];
        Random rand = new Random();

        for (Button btn : meglevok) {
            int ertek;
            do {
                ertek = rand.nextInt(szavak.size());
            } while (mennyiseg[ertek] >= 2);
            mennyiseg[ertek]++;
            btn.setText("?");
            valasz = szavak.get(ertek);
            obj.put(valasz, btn);
        }

        meglevok.clear();
        words.clear();
        obj.clear();
    }

    public Button getButton(){
        return button;
    }

    public Button getNewButton(){
        return new_game;
    }
}
