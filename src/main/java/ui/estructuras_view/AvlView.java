package ui.estructuras_view;

import clases.Productos;
import clases.Sucursal;
import estructuras.avl.ArbolAVL;
import estructuras.avl.NodoAVL;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ui.view.AppState;

import java.util.List;

public class AvlView extends VBox {

    private AppState state;
    private Canvas           canvas;
    private ComboBox<String> cmbSucursal;
    private Label            lblInfo;
    private Slider           sliderZoom;

    // Radio e interlineado se calculan dinámicamente según cantidad de nodos
    private static final int RADIO_MAX  = 24;
    private static final int RADIO_MIN  = 6;
    private static final int SEP_V_BASE = 70;

    public AvlView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Visualización — Árbol AVL");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("AVL global (todos)");
        recargarSucursales();

        Button btnDibujar = new Button("Dibujar AVL");
        btnDibujar.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white; -fx-padding: 6 14;"
        );

        Button btnInOrder = new Button("Ver inOrder");
        btnInOrder.setStyle(
                "-fx-background-color: #8e44ad;" +
                        "-fx-text-fill: white; -fx-padding: 6 14;"
        );

        // Slider de zoom
        sliderZoom = new Slider(0.3, 1.5, 1.0);
        sliderZoom.setShowTickLabels(true);
        sliderZoom.setShowTickMarks(true);
        sliderZoom.setMajorTickUnit(0.3);
        sliderZoom.setPrefWidth(160);
        Label lblZoom = new Label("Zoom:");

        lblInfo = new Label(
                "Rojo = raíz  |  Verde = hoja  |  Azul = nodo interno  |  h = altura del nodo"
        );
        lblInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        HBox controles = new HBox(10,
                new Label("Sucursal:"), cmbSucursal,
                btnDibujar, btnInOrder,
                new Separator(), lblZoom, sliderZoom
        );
        controles.setStyle("-fx-alignment: center-left;");

        // Canvas con tamaño grande por defecto; se redimensiona en dibujar()
        canvas = new Canvas(2000, 800);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(680);
        scroll.setFitToWidth(false);

        TextArea txtInOrder = new TextArea();
        txtInOrder.setEditable(false);
        txtInOrder.setPrefHeight(90);
        txtInOrder.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnDibujar.setOnAction(e -> {
            recargarSucursales();
            ArbolAVL<Productos> avl = obtenerAVL();
            if (avl != null) {
                int n = avl.inOrder().size();
                lblInfo.setText(
                        "Nodos: " + n +
                                "  |  Altura: " + avl.getHeight() +
                                "  |  O(log n) ≈ " +
                                String.format("%.1f", Math.log(Math.max(n, 1)) / Math.log(2)) +
                                " pasos teóricos"
                );
                dibujar(avl);
            }
        });

        // Re-dibujar cuando cambia zoom (si ya hay árbol)
        sliderZoom.valueProperty().addListener((obs, o, n) -> {
            ArbolAVL<Productos> avl = obtenerAVL();
            if (avl != null) dibujar(avl);
        });

        btnInOrder.setOnAction(e -> {
            ArbolAVL<Productos> avl = obtenerAVL();
            if (avl == null) return;
            txtInOrder.clear();
            List<Productos> lista = avl.inOrder();
            StringBuilder sb = new StringBuilder(
                    "InOrder (" + lista.size() + " productos): "
            );
            for (int i = 0; i < lista.size(); i++) {
                sb.append(lista.get(i).getName());
                if (i < lista.size() - 1) sb.append(" → ");
            }
            txtInOrder.setText(sb.toString());
        });

        this.getChildren().addAll(
                titulo, new Separator(),
                controles, lblInfo,
                scroll, txtInOrder
        );
    }

    // ─────────────────────────────────────────
    private ArbolAVL<Productos> obtenerAVL() {
        String sel = cmbSucursal.getValue();
        if (sel == null) {
            ArbolAVL<Productos> avl = state.getAvlGlobal();
            if (avl.isEmpty()) { mostrarVacio("AVL global vacío"); return null; }
            return avl;
        }
        int id = Integer.parseInt(sel.split(" - ")[0].trim());
        Sucursal s = state.getCargaCSV().buscarSucursal(id);
        if (s == null || s.getAvlNombre().isEmpty()) {
            mostrarVacio("AVL vacío para esta sucursal");
            return null;
        }
        return s.getAvlNombre();
    }

    private void recargarSucursales() {
        String sel = cmbSucursal.getValue();
        cmbSucursal.getItems().clear();
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmbSucursal.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }
        cmbSucursal.setValue(sel);
    }

    // ─────────────────────────────────────────
    // DIBUJAR — escala dinámica
    // ─────────────────────────────────────────
    private void dibujar(ArbolAVL<Productos> avl) {
        double zoom  = sliderZoom.getValue();
        int    alt   = avl.getHeight();
        int    n     = avl.inOrder().size();

        // Radio proporcional: menos nodos → más grande
        int radio = (int) Math.max(RADIO_MIN,
                Math.min(RADIO_MAX, RADIO_MAX - (n / 30.0))) ;
        radio = (int)(radio * zoom);

        int sepV  = (int)(SEP_V_BASE * zoom);

        // Ancho necesario: hojas en el nivel más profundo = 2^alt
        double anchoNec = Math.pow(2, alt) * (radio * 2 + 8) * zoom;
        double altoNec  = (alt + 1) * sepV + 80;

        double canvasW = Math.max(2000, anchoNec);
        double canvasH = Math.max(600,  altoNec);

        canvas.setWidth(canvasW);
        canvas.setHeight(canvasH);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);

        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, canvasW, canvasH);

        dibujarNodo(gc, avl.getRoot(),
                canvasW / 2, 50,
                canvasW / 4,
                true, radio, sepV);
    }

    private void dibujarNodo(GraphicsContext gc,
                             NodoAVL<Productos> nodo,
                             double x, double y,
                             double offset,
                             boolean esRaiz, int radio, int sepV) {
        if (nodo == null) return;

        boolean esHoja = (nodo.left == null && nodo.right == null);

        gc.setLineWidth(1.2);
        if (nodo.left != null) {
            double hx = x - offset, hy = y + sepV;
            gc.setStroke(Color.web("#bdc3c7"));
            gc.strokeLine(x, y, hx, hy);
            dibujarNodo(gc, nodo.left, hx, hy, offset / 2, false, radio, sepV);
        }
        if (nodo.right != null) {
            double hx = x + offset, hy = y + sepV;
            gc.setStroke(Color.web("#bdc3c7"));
            gc.strokeLine(x, y, hx, hy);
            dibujarNodo(gc, nodo.right, hx, hy, offset / 2, false, radio, sepV);
        }

        Color color = esRaiz     ? Color.web("#e74c3c")
                : esHoja     ? Color.web("#27ae60")
                  :               Color.web("#2980b9");

        // Sombra
        gc.setFill(Color.web("#00000018"));
        gc.fillOval(x - radio + 2, y - radio + 2, radio * 2, radio * 2);

        gc.setFill(color);
        gc.fillOval(x - radio, y - radio, radio * 2, radio * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(x - radio, y - radio, radio * 2, radio * 2);

        // Texto dentro del nodo — fuente proporcional al radio
        String texto = obtenerTexto(nodo.producto, radio);
        gc.setFill(Color.WHITE);
        double fontSize = Math.max(7, radio * 0.45);
        gc.setFont(Font.font(fontSize));
        double tw = texto.length() * fontSize * 0.55;
        gc.fillText(texto, x - tw / 2, y + fontSize * 0.35);

        // Altura del nodo
        gc.setFill(Color.web("#555"));
        gc.setFont(Font.font(Math.max(7, fontSize - 1)));
        gc.fillText("h=" + nodo.altura, x - 8, y + radio + 11);
    }

    private String obtenerTexto(Productos p, int radio) {
        if (p == null) return "?";
        int maxChars = Math.max(2, radio / 4 + 3);
        String nom = p.getName();
        return nom.length() > maxChars ? nom.substring(0, maxChars - 1) + "." : nom;
    }

    private void mostrarVacio(String msg) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.GRAY);
        gc.setFont(Font.font(14));
        gc.fillText(msg, 400, 200);
    }
}