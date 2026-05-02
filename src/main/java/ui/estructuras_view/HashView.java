package ui.estructuras_view;

import clases.Sucursal;
import estructuras.hash.TablaHash;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ui.view.AppState;
import ui.view.PanelExportacion;
import utils.ExportarEstructuras;

public class HashView extends VBox {

    private AppState state;
    private Canvas canvas;
    private ComboBox<String> cmbSucursal;
    private Label lblFactor;

    // Dimensiones base; se ajustan para acomodar todos los buckets
    private static final int BUCKET_H_BASE = 28;
    private static final int BUCKET_W_BASE = 160;

    public HashView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Visualización — Tabla Hash");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("Seleccionar sucursal");
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmbSucursal.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }

        Button btnDibujar = new Button("Dibujar Hash");
        btnDibujar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 6 14;");

        // Toggle para mostrar/ocultar buckets vacíos
        CheckBox chkMostrarVacios = new CheckBox("Mostrar buckets vacíos");
        chkMostrarVacios.setSelected(false);

        lblFactor = new Label("Factor de carga: —");
        lblFactor.setStyle("-fx-font-weight: bold;");

        Label lblInfo = new Label(
                "Colisiones resueltas por chaining  |  Búsqueda O(1) promedio"
        );
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        HBox controles = new HBox(10,
                new Label("Sucursal:"), cmbSucursal,
                btnDibujar, chkMostrarVacios, lblFactor
        );
        controles.setStyle("-fx-alignment: center-left;");

        // Canvas con scroll — dimensiones se calculan en dibujar()
        canvas = new Canvas(1400, 800);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(680);
        scroll.setFitToWidth(false);

        HBox leyenda = crearLeyenda();

        btnDibujar.setOnAction(e -> dibujar(chkMostrarVacios.isSelected()));
        chkMostrarVacios.setOnAction(e -> dibujar(chkMostrarVacios.isSelected()));

        this.getChildren().addAll(
                titulo, new Separator(),
                controles, lblInfo, leyenda, scroll, panelExport
        );
    }

    private void dibujar(boolean mostrarVacios) {
        String sel = cmbSucursal.getValue();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        TablaHash tabla = null;
        if (sel != null) {
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s != null) tabla = s.getTablaHash();
        }

        if (tabla == null || tabla.isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font(14));
            gc.fillText("Tabla Hash vacía — carga productos primero", 300, 200);
            return;
        }

        int capacidad = tabla.getCapacidad();
        int elementos = tabla.size();
        double factorCarga = tabla.factorCarga();

        lblFactor.setText(String.format(
                "Factor de carga: %.3f  (%d elementos / %d buckets)",
                factorCarga, elementos, capacidad
        ));

        // ── Calcular layout dinámico ──
        // Elegir cuántas columnas según la capacidad
        int maxCols;
        if (capacidad <= 20) maxCols = 2;
        else if (capacidad <= 50) maxCols = 3;
        else if (capacidad <= 100) maxCols = 4;
        else if (capacidad <= 200) maxCols = 5;
        else maxCols = 6;

        // Escalar tamaño de bucket para que quepan bien
        double escala = Math.max(0.45, Math.min(1.0, 500.0 / capacidad));
        int bH = (int) (BUCKET_H_BASE * escala);
        int bW = (int) (BUCKET_W_BASE * escala);
        int padX = 15;
        int padY = 15;
        int gapX = 8;
        int gapY = 5;

        // Filas necesarias (sólo buckets no-vacíos, o todos si toggle)
        int bucketsMostrados = mostrarVacios ? capacidad : (capacidad - contarVacios(tabla, capacidad));
        int filas = (int) Math.ceil((double) bucketsMostrados / maxCols);

        double canvasW = maxCols * (bW + gapX) + padX * 2 + 200; // +200 para cadenas de colisión
        double canvasH = filas * (bH + gapY) + padY * 2 + 80;

        canvas.setWidth(Math.max(1400, canvasW));
        canvas.setHeight(Math.max(600, canvasH));

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int col = 0;
        int fila = 0;
        int vacios = 0;
        int colisiones = 0;

        double fontSize = Math.max(8, bH * 0.36);

        for (int i = 0; i < capacidad; i++) {
            int tam = tabla.tamanioBucket(i);

            if (tam == 0) {
                vacios++;
                if (!mostrarVacios) continue;
            }

            if (tam > 1) colisiones++;

            double x = padX + col * (bW + gapX);
            double y = padY + fila * (bH + gapY);

            // Color del bucket
            Color color;
            if (tam == 0) color = Color.web("#ecf0f1");   // vacío — gris claro
            else if (tam == 1) color = Color.web("#27ae60");   // sin colisión — verde
            else color = Color.web("#e74c3c");   // colisión — rojo

            // Bucket principal
            gc.setFill(color);
            gc.fillRoundRect(x, y, bW, bH, 5, 5);
            gc.setStroke(tam == 0 ? Color.web("#bdc3c7") : Color.WHITE);
            gc.setLineWidth(0.8);
            gc.strokeRoundRect(x, y, bW, bH, 5, 5);

            // Texto
            gc.setFill(tam == 0 ? Color.web("#95a5a6") : Color.WHITE);
            gc.setFont(Font.font(fontSize));
            String txt;
            if (tam == 0) {
                txt = String.format("[%d] vacío", i);
            } else {
                txt = String.format("[%d] %d elem%s",
                        i, tam, tam > 1 ? " ⚠" : "");
            }
            // Truncar si no cabe
            while (txt.length() * fontSize * 0.58 > bW - 6 && txt.length() > 5)
                txt = txt.substring(0, txt.length() - 1);
            gc.fillText(txt, x + 4, y + bH / 2.0 + fontSize * 0.35);

            // Cadena de colisión (cuadraditos a la derecha)
            if (tam > 1) {
                for (int k = 1; k < Math.min(tam, 6); k++) {
                    double cx2 = x + bW + 3 + (k - 1) * (int) (bH * 1.1);
                    gc.setFill(Color.web("#c0392b"));
                    gc.fillRoundRect(cx2, y, bH, bH, 4, 4);
                    gc.setFill(Color.WHITE);
                    gc.setFont(Font.font(Math.max(7, fontSize - 1)));
                    gc.fillText(String.valueOf(k), cx2 + bH * 0.3, y + bH / 2.0 + fontSize * 0.3);
                }
                if (tam > 6) {
                    double cx2 = x + bW + 3 + 5 * (int) (bH * 1.1);
                    gc.setFill(Color.web("#7f8c8d"));
                    gc.setFont(Font.font(Math.max(7, fontSize - 1)));
                    gc.fillText("+" + (tam - 6), cx2, y + bH / 2.0 + fontSize * 0.3);
                }
            }

            col++;
            if (col >= maxCols) {
                col = 0;
                fila++;
            }
        }

        // ── Resumen al final ──
        double resY = padY + (fila + 1) * (bH + gapY) + 20;
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font(12));
        gc.fillText(String.format(
                "Total: %d buckets  |  Ocupados: %d  |  Vacíos: %d  |  Buckets con colisión: %d",
                capacidad, capacidad - vacios, vacios, colisiones
        ), padX, resY);
    }

    private int contarVacios(TablaHash tabla, int capacidad) {
        int v = 0;
        for (int i = 0; i < capacidad; i++)
            if (tabla.tamanioBucket(i) == 0) v++;
        return v;
    }

    private HBox crearLeyenda() {
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(5));
        String[][] items = {
                {"Sin colisión", "#27ae60"},
                {"Con colisión ⚠", "#e74c3c"},
                {"Vacío", "#ecf0f1"}
        };
        for (String[] item : items) {
            Canvas c = new Canvas(14, 14);
            GraphicsContext gc = c.getGraphicsContext2D();
            gc.setFill(Color.web(item[1]));
            gc.fillRoundRect(0, 0, 14, 14, 4, 4);
            Label l = new Label(item[0]);
            l.setStyle("-fx-font-size: 12px;");
            hbox.getChildren().addAll(c, l);
        }
        return hbox;
    }

    PanelExportacion panelExport = new PanelExportacion(
            "Tabla Hash",
            () -> {
                String sel = cmbSucursal.getValue();
                if (sel == null) return "";
                int id = Integer.parseInt(sel.split(" - ")[0]);
                Sucursal s = state.getCargaCSV().buscarSucursal(id);
                if (s == null) return "";
                return ExportarEstructuras.hashToDot(
                        s.getTablaHash(), "Hash_S" + id
                );
            },
            (ruta, fmt) -> {
                String sel = cmbSucursal.getValue();
                if (sel == null) return false;
                int id = Integer.parseInt(sel.split(" - ")[0]);
                Sucursal s = state.getCargaCSV().buscarSucursal(id);
                if (s == null) return false;
                return ExportarEstructuras.exportarHashImagen(
                        s.getTablaHash(), ruta, fmt
                );
            }
    );
}