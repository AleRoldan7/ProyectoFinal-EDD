package ui.estructuras_view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import clases.Sucursal;
import ui.view.AppState;

public class CargaCSVView extends VBox {

    private AppState state;
    private TextArea log;

    public CargaCSVView(AppState state) {
        this.state = state;
        this.setSpacing(12);
        this.setPadding(new Insets(20));

        Label titulo = new Label("Carga de archivos CSV");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Fila de cada botón con su label de estado
        HBox filaSuc  = crearFila("Sucursales.csv",   this::cargarSucursales);
        HBox filaProd = crearFila("Productos.csv",    this::cargarProductos);
        HBox filaCon  = crearFila("Conexiones.csv",   this::cargarConexiones);

        log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(350);
        log.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        log.setPromptText("Los resultados aparecerán aquí...");

        Button btnLimpiar = new Button("Limpiar log");
        btnLimpiar.setOnAction(e -> log.clear());

        this.getChildren().addAll(
                titulo,
                new Separator(),
                filaSuc, filaProd, filaCon,
                new Separator(),
                new Label("Log de carga:"),
                log,
                btnLimpiar
        );
    }

    private HBox crearFila(String nombre, Runnable accion) {
        Label lbl    = new Label(nombre);
        lbl.setPrefWidth(150);
        Button btn   = new Button("Seleccionar archivo");
        Label estado = new Label("⏳ Sin cargar");
        estado.setStyle("-fx-text-fill: #e67e22;");

        btn.setOnAction(e -> {
            accion.run();
            estado.setText("✅ Cargado");
            estado.setStyle("-fx-text-fill: #27ae60;");
        });

        HBox fila = new HBox(12, lbl, btn, estado);
        fila.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-padding: 10;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );
        return fila;
    }

    private String elegirArchivo(String titulo) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titulo);
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );
        java.io.File f = fc.showOpenDialog(new Stage());
        return (f != null) ? f.getAbsolutePath() : null;
    }

    private void cargarSucursales() {
        String ruta = elegirArchivo("Seleccionar sucursales.csv");
        if (ruta == null) return;
        state.getCargaCSV().cargarSucursales(ruta);
        // Registrar sucursales en el grafo
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            state.getGrafo().agregarSucursal(s.getIdSucursal());
        }
        log.appendText("✅ Sucursales: "
                + state.getCargaCSV().getTotalSucursales()
                + " cargadas\n");
    }

    private void cargarProductos() {
        String ruta = elegirArchivo("Seleccionar productos.csv");
        if (ruta == null) return;
        state.getCargaCSV().cargarProductos(ruta);
        // Insertar en AVL global
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            for (Object obj : s.getLista().toList()) {
                clases.Productos p = (clases.Productos) obj;
                state.getAvlGlobal().insert(p);
            }
        }
        log.appendText("✅ Productos cargados\n");
        log.appendText("   (ver errors.log para detalles)\n");
    }

    private void cargarConexiones() {
        String ruta = elegirArchivo("Seleccionar conexiones.csv");
        if (ruta == null) return;
        state.getGrafo().cargarConexionesCSV(ruta);
        log.appendText("✅ Conexiones cargadas: "
                + state.getGrafo().getTotalNodos()
                + " nodos en el grafo\n");
    }
}
