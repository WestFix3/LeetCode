import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cards {
    public static List<String> words = new ArrayList<>();
    public static Map<String, Button> obj = new HashMap<>();
    Button button;
    String kerdes;
    String valasz;

    public Cards(String tartalom, String valasz){
        button = new Button(tartalom);
        button.setPrefSize(100, 50);
        this.kerdes = tartalom;
        this.valasz = valasz;
    }

    public void setOn(){
        //Irjuk ki mi van a list és hashmapben
        button.setOnAction(
                e-> {
                    if(words.size() < 2){
                        if(button.getText().equals(kerdes)){
                            button.setText(valasz);
                            words.add(valasz);
                            obj.put(valasz, getButton());
                        }else{
                            button.setText(kerdes);
                            for(String ertek : words){
                                if(ertek.equals(valasz)){
                                    words.remove(valasz);
                                    obj.remove(valasz);
                                }
                            }
                        }
                    }else{
                        button.setText(kerdes);
                        for(String ertek : words){
                            if(ertek.equals(valasz)){
                                words.remove(valasz);
                                obj.remove(valasz);
                            }
                        }
                    }
                    if(words.size() == 2){
                        if(words.get(0).equals(words.get(1))){
                            obj.get(words.get(0)).setText("");
                            obj.get(words.get(1)).setText("");
                        }
                    }
                }
        );
    }

    public Button getButton(){
        return button;
    }
}
