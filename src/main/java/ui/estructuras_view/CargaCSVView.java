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

    private Label estadoSucursales;
    private Label estadoProductos;
    private Label estadoConexiones;

    public CargaCSVView(AppState state) {
        this.state = state;
        this.setSpacing(12);
        this.setPadding(new Insets(20));

        Label titulo = new Label("Carga de archivos CSV");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        estadoSucursales = etiquetaEstado();
        estadoProductos = etiquetaEstado();
        estadoConexiones = etiquetaEstado();

        HBox filaSuc = crearFila("Sucursales.csv", estadoSucursales, this::cargarSucursales);
        HBox filaProd = crearFila("Productos.csv", estadoProductos, this::cargarProductos);
        HBox filaCon = crearFila("Conexiones.csv", estadoConexiones, this::cargarConexiones);

        log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(350);
        log.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        log.setPromptText("Los resultados aparecerán aquí...");

        Button btnLimpiar = new Button("Limpiar log");
        btnLimpiar.setOnAction(e -> log.clear());

        this.getChildren().addAll(titulo, new Separator(), filaSuc, filaProd, filaCon, new Separator(), new Label("Log de carga:"), log, btnLimpiar);
    }

    private HBox crearFila(String nombre, Label estado, Runnable accion) {
        Label lbl = new Label(nombre);
        lbl.setPrefWidth(150);

        Button btn = new Button("Seleccionar archivo");
        btn.setOnAction(e -> accion.run());

        HBox fila = new HBox(12, lbl, btn, estado);
        fila.setStyle("-fx-background-color: #f8f9fa;" + "-fx-padding: 10;" + "-fx-border-radius: 6;" + "-fx-background-radius: 6;");
        return fila;
    }

    private Label etiquetaEstado() {
        Label l = new Label("Sin cargar");
        l.setStyle("-fx-text-fill: #e67e22;");
        return l;
    }

    private void cargarSucursales() {
        String ruta = elegirArchivo("Seleccionar sucursales.csv");
        if (ruta == null) {
            appendLog("[Sucursales] Operación cancelada por el usuario.");
            return;
        }

        appendLog("[Sucursales] Leyendo: " + ruta);
        try {
            int antesde = state.getCargaCSV().getTotalSucursales();
            state.getCargaCSV().cargarSucursales(ruta);
            int cargadas = state.getCargaCSV().getTotalSucursales();

            if (cargadas == 0) {
                marcarError(estadoSucursales, "Sin datos");
                appendLog("[Sucursales] No se cargó ninguna sucursal. " + "Verifica que el CSV tenga el formato correcto " + "(columnas: id,nombre).");
                mostrarAlerta(Alert.AlertType.WARNING, "Sucursales", "Verifica que el CSV tenga el formato correcto");
                return;
            }

            int nodosAgregados = 0;
            for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
                state.getGrafo().agregarSucursal(s.getIdSucursal());
                nodosAgregados++;
            }

            int nuevas = cargadas - antesde;
            marcarOk(estadoSucursales, cargadas + " cargadas");
            appendLog(String.format("[Sucursales] %d sucursal(es) en total (%d nuevas). " + "%d nodo(s) registrados en el grafo.", cargadas, nuevas, nodosAgregados));
            mostrarAlerta(Alert.AlertType.CONFIRMATION, "Sucursales", "Se cargaron las sucursales: " + cargadas);

        } catch (Exception ex) {
            marcarError(estadoSucursales, "Error");
            appendLog("[Sucursales] Error inesperado: " + ex.getMessage());
            appendLog("             Causa: " + (ex.getCause() != null ? ex.getCause().getMessage() : "desconocida"));
        }
    }

    private void cargarProductos() {
        String ruta = elegirArchivo("Seleccionar productos.csv");
        if (ruta == null) {
            appendLog("[Productos] Operación cancelada por el usuario.");
            return;
        }

        if (state.getCargaCSV().getTotalSucursales() == 0) {
            marcarError(estadoProductos, "Sin sucursales");
            appendLog("[Productos] Debes cargar Sucursales.csv primero. " + "Los productos necesitan una sucursal de destino.");
            mostrarAlerta(Alert.AlertType.WARNING, "Productos", "Se deben de cargar sucursales");
            return;
        }

        appendLog("[Productos] Leyendo: " + ruta);
        try {
            state.getCargaCSV().cargarProductos(ruta);

            int totalProductos = 0;
            int sucSinProd = 0;

            for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
                int cantidad = s.getLista().size();
                if (cantidad == 0) {
                    sucSinProd++;
                    appendLog(String.format("[Productos]  Sucursal %d (%s): sin productos asignados.", s.getIdSucursal(), s.getNameSucursal()));
                } else {
                    totalProductos += cantidad;
                    for (Object obj : s.getLista().toList()) {
                        clases.Productos p = (clases.Productos) obj;
                        state.getAvlGlobal().insert(p);
                    }
                }
            }


            if (totalProductos == 0) {
                marcarError(estadoProductos, "Sin datos");
                appendLog("[Productos] No se asignó ningún producto a ninguna sucursal. " + "Revisa el formato del CSV y que los IDs de sucursal coincidan.");
                return;
            }

            marcarOk(estadoProductos, totalProductos + " productos");
            appendLog(String.format("[Productos] %d producto(s) cargados e indexados en el AVL global.", totalProductos));
            mostrarAlerta(Alert.AlertType.CONFIRMATION, "Productos", "Se cargaron lo productos: " + totalProductos);

            if (sucSinProd > 0) {
                appendLog(String.format("[Productos]   %d sucursal(es) quedaron sin productos " + "(IDs desconocidos o filas con errores).", sucSinProd));
                appendLog("Revisa errors.log para más detalles.");
            }

        } catch (Exception ex) {
            marcarError(estadoProductos, "Error");
            appendLog("[Productos] ✘ Error inesperado: " + ex.getMessage());
            appendLog("             Causa: " + (ex.getCause() != null ? ex.getCause().getMessage() : "desconocida"));
        }
    }

    private void cargarConexiones() {
        String ruta = elegirArchivo("Seleccionar conexiones.csv");
        if (ruta == null) {
            appendLog("[Conexiones] Operación cancelada por el usuario.");
            return;
        }

        if (state.getGrafo().getTotalNodos() == 0) {
            marcarError(estadoConexiones, "Sin nodos");
            appendLog("[Conexiones] Debes cargar Sucursales.csv primero. " + "El grafo no tiene nodos a los que conectar.");
            mostrarAlerta(Alert.AlertType.ERROR, "Conexiones", "Se deben de cargar conexiones");
            return;
        }

        appendLog("[Conexiones] Leyendo: " + ruta);
        try {
            int nodosAntes = state.getGrafo().getTotalNodos();
            state.getGrafo().cargarConexionesCSV(ruta);
            int nodosDespues = state.getGrafo().getTotalNodos();

            if (nodosDespues == 0) {
                marcarError(estadoConexiones, "Sin conexiones");
                appendLog("[Conexiones] No se cargó ninguna conexión. " + "Verifica el CSV (columnas esperadas: origen,destino,tiempo,costo).");
                mostrarAlerta(Alert.AlertType.WARNING, "Conexiones", "No se cargo el archivo de conexiones");
                return;
            }

            marcarOk(estadoConexiones, nodosDespues + " nodos");
            appendLog(String.format("[Conexiones] Grafo actualizado: %d nodo(s) conectados.", nodosDespues));
            mostrarAlerta(Alert.AlertType.CONFIRMATION, "Conexiones", "Se cargaron conexiones: " + nodosDespues);
            if (nodosDespues < nodosAntes) {
                appendLog(String.format("[Conexiones]   %d nodo(s) quedaron sin conexiones " + "(sucursales aisladas).", nodosAntes - nodosDespues));
            }

        } catch (Exception ex) {
            marcarError(estadoConexiones, "Error");
            appendLog("[Conexiones] ✘ Error inesperado: " + ex.getMessage());
            appendLog("             Causa: " + (ex.getCause() != null ? ex.getCause().getMessage() : "desconocida"));
        }
    }

    private String elegirArchivo(String titulo) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titulo);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File f = fc.showOpenDialog(new Stage());
        return (f != null) ? f.getAbsolutePath() : null;
    }

    private void marcarOk(Label lbl, String texto) {
        lbl.setText("Bien: " + texto);
        lbl.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void marcarError(Label lbl, String texto) {
        lbl.setText("Error: " + texto);
        lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    private void appendLog(String mensaje) {
        log.appendText(mensaje + "\n");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}