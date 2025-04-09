import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class Grid extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Létrehozunk egy VBox-ot a sorokhoz
        VBox vBox = new VBox();

        // Létrehozunk néhány HBox-ot az oszlopokhoz
        HBox hBox1 = new HBox();
        HBox hBox2 = new HBox();
        HBox hBox3 = new HBox();
        HBox hBox4 = new HBox();

        List<Cards> cardsList = new ArrayList<>();
        List<String> szavak = List.of("alma", "banan", "cica", "kutya", "macska", "korte", "dinnye", "eper", "lo", "hal");
        int[] mennyiseg = new int[]{0,0,0,0,0,0,0,0,0,0};

        for(int i=0; i<20; i++){
            Random rand = new Random();
            int ertek;
            do{
                ertek = rand.nextInt(10);
            }while(mennyiseg[ertek] > 1);
            mennyiseg[ertek] = mennyiseg[ertek] + 1;
            cardsList.add(new Cards("?", szavak.get(ertek)));
        }

        for(int i=0; i<20; i++){
            cardsList.get(i).setOn();
        }


        // Hozzáadjuk a gombokat az HBox-okhoz
        for(int i=0; i<20; i++){
            if(i<5){
                System.out.println("1: " + i);
                hBox1.getChildren().add(cardsList.get(i).getButton());
            }else
            if(i<10){
                System.out.println("2: " + i);
                hBox2.getChildren().add(cardsList.get(i).getButton());
            }else
            if(i<15){
                System.out.println("3: " + i);
                hBox3.getChildren().add(cardsList.get(i).getButton());
            }else
            if(i<20){
                System.out.println("4: " + i);
                hBox4.getChildren().add(cardsList.get(i).getButton());
            }
        }

        // Hozzáadjuk az HBox-okat a VBox-hoz
        vBox.getChildren().addAll(hBox1, hBox2, hBox3, hBox4);

        // Létrehozunk egy Scene-t és beállítjuk a VBox-ot
        Scene scene = new Scene(vBox, 500, 200);

        // Beállítjuk a Stage-t és megjelenítjük
        primaryStage.setTitle("JavaFX Table Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
