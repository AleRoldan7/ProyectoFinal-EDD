package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ViewMain extends BorderPane {

    private VBox menu;
    private StackPane contenido;

    public ViewMain() {

        crearMenu();
        crearContenido();

        this.setLeft(menu);
        this.setCenter(contenido);
    }

    private void crearMenu() {

        menu = new VBox(10);
        menu.setPadding(new Insets(10));
        menu.setStyle("-fx-background-color: #2c3e50;");

        Button btnProductos = crearBoton("Productos");
        Button btnSucursales = crearBoton("Sucursales");
        Button btnCarga = crearBoton("Cargar CSV");
        Button btnAVL = crearBoton("Árbol AVL");
        Button btnB = crearBoton("Árbol B");
        Button btnBPlus = crearBoton("Árbol B+");
        Button btnHash = crearBoton("Hash");
        Button btnGrafo = crearBoton("Grafo");

        btnProductos.setOnAction(e -> mostrarVista(new ProductosView()));
        btnSucursales.setOnAction(e -> mostrarVista(new Label("Vista Sucursales")));
        btnCarga.setOnAction(e -> mostrarVista(new Label("Carga CSV")));
        btnAVL.setOnAction(e -> mostrarVista(new Label("Visualización AVL")));
        btnB.setOnAction(e -> mostrarVista(new Label("Visualización B")));
        btnBPlus.setOnAction(e -> mostrarVista(new Label("Visualización B+")));
        btnHash.setOnAction(e -> mostrarVista(new Label("Tabla Hash")));
        btnGrafo.setOnAction(e -> mostrarVista(new Label("Grafo")));

        menu.getChildren().addAll(
                btnProductos, btnSucursales, btnCarga,
                btnAVL, btnB, btnBPlus, btnHash, btnGrafo
        );
    }

    private Button crearBoton(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        return btn;
    }

    private void crearContenido() {
        contenido = new StackPane();
        contenido.setPadding(new Insets(20));
        contenido.getChildren().add(new Label("Bienvenido al sistema"));
    }

    private void mostrarVista(javafx.scene.Node nodo) {
        contenido.getChildren().clear();
        contenido.getChildren().add(nodo);
    }
}
