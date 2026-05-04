package ui.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ui.estructuras_view.AvlView;
import ui.estructuras_view.*;

public class ViewMain extends BorderPane {

    private VBox menu;
    private StackPane contenido;
    private AppState state;

    public ViewMain() {
        state = AppState.getInstance();
        crearMenu();
        crearContenido();
        this.setLeft(menu);
        this.setCenter(contenido);
    }

    private void crearMenu() {
        menu = new VBox(10);
        menu.setPadding(new Insets(15));
        menu.setPrefWidth(180);
        menu.setStyle("-fx-background-color: #2c3e50;");

        Label titulo = new Label("SUPERMERCADO");
        titulo.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #7f8c8d;");

        Button btnCarga = crearBoton("Cargar CSV");
        Button btnProductos = crearBoton("Productos");
        Button btnSucursales = crearBoton("Sucursales");
        Button btnConexiones = crearBoton("Conexiones");
        Button btnGrafo = crearBoton("Grafo / Rutas");
        Button btnAVL = crearBoton("Árbol AVL");
        Button btnB = crearBoton("Árbol B");
        Button btnBPlus = crearBoton("Árbol B+");
        Button btnHash = crearBoton("Tabla Hash");
        Button btnEnlazada = crearBoton("Lista Enlazada");
        Button btnTransferencia = crearBoton("Transferencia");
        Button btnRendimiento = crearBoton("Rendimiento");
        Button btnEliminar = crearBoton("Eliminar");

        btnCarga.setOnAction(e ->
                mostrarVista(new CargaCSVView(state))
        );
        btnProductos.setOnAction(e ->
                mostrarVista(new ProductosView(state))
        );
        btnSucursales.setOnAction(e ->
                mostrarVista(new SucursalesView(state))
        );
        btnConexiones.setOnAction(e ->
                mostrarVista(new ConexionView(state))
        );
        btnGrafo.setOnAction(e ->
                mostrarVista(new GrafoAnimadoView(state))
        );

        btnAVL.setOnAction(e ->
                mostrarVista(new AvlView(state))
        );
        btnB.setOnAction(e ->
                mostrarVista(new ArbolBView(state))
        );
        btnBPlus.setOnAction(e ->
                mostrarVista(new ArbolBPlusView(state))
        );
        btnHash.setOnAction(e ->
                mostrarVista(new HashView(state))
        );

        btnEnlazada.setOnAction(e ->
                mostrarVista(new ListaEnlazadaView(state))
        );
        btnTransferencia.setOnAction(e ->
                mostrarVista(new TransferenciaView(state))
        );

        btnRendimiento.setOnAction(e ->
                mostrarVista(new RendimientoView(state))
        );

        btnEliminar.setOnAction(e ->
            mostrarVista(new EliminacionView(state))
        );

        menu.getChildren().addAll(
                titulo, sep,
                btnCarga, btnProductos, btnSucursales, btnConexiones,
                new Separator(),
                btnGrafo,
                new Separator(),
                btnAVL, btnB, btnBPlus, btnHash, btnEnlazada,
                new Separator(),
                btnTransferencia,
                btnRendimiento,
                btnEliminar

        );
    }

    private Button crearBoton(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
                "-fx-background-color: #34495e;" +
                        "-fx-text-fill: white;" +
                        "-fx-alignment: center-left;" +
                        "-fx-padding: 8 12;"
        );
        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: #1abc9c;" +
                                "-fx-text-fill: white;" +
                                "-fx-alignment: center-left;" +
                                "-fx-padding: 8 12;"
                )
        );
        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: #34495e;" +
                                "-fx-text-fill: white;" +
                                "-fx-alignment: center-left;" +
                                "-fx-padding: 8 12;"
                )
        );
        return btn;
    }

    private void crearContenido() {
        contenido = new StackPane();
        contenido.setPadding(new Insets(20));
        Label bienvenida = new Label("Bienvenido al sistema\nCarga los archivos CSV para comenzar");
        bienvenida.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        contenido.getChildren().add(bienvenida);
    }

    private void mostrarVista(javafx.scene.Node nodo) {
        contenido.getChildren().clear();
        contenido.getChildren().add(nodo);
    }
}
