package ui.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppPrincipal extends Application {

    public void start(Stage stage) {

        ViewMain viewMain = new ViewMain();

        Scene scene = new Scene(viewMain, 1000,600);

        stage.setTitle("PROYECTO FINAL EDD - Gestión de Catálogo de " +
                "Productos de Supermercado");

        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}