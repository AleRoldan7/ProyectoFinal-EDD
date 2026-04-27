package ui.estructuras_view;

import clases.Sucursal;
import estructuras.arbolB.ArbolB;
import estructuras.arbolB.NodoB;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ui.view.AppState;

import java.util.List;

public class ArbolBView extends VBox {

    private AppState state;
    private Canvas   canvas;
    private ComboBox<String> cmbSucursal;
    private Label    lblInfo;

    // Dimensiones base del nodo individual — se escalan si hay muchos nodos
    private static final int NODO_H_BASE = 34;
    private static final int NODO_W_BASE = 120;
    private static final int SEP_V_BASE  = 80;

    public ArbolBView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Visualización — Árbol B");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("Seleccionar sucursal");
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmbSucursal.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }

        Button btnDibujar = new Button("Dibujar Árbol B");
        btnDibujar.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-padding: 6 14;");

        lblInfo = new Label("Árbol B indexado por fecha de caducidad  |  Búsqueda por rango: O(log n)");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        HBox controles = new HBox(10,
                new Label("Sucursal:"), cmbSucursal, btnDibujar, lblInfo
        );

        // Canvas inicial grande; se redimensiona al dibujar
        canvas = new Canvas(3000, 700);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(680);
        scroll.setFitToWidth(false);

        HBox leyenda = crearLeyenda();

        btnDibujar.setOnAction(e -> dibujar());

        this.getChildren().addAll(
                titulo, new Separator(),
                controles, leyenda, scroll
        );
    }

    @SuppressWarnings("unchecked")
    private void dibujar() {
        String sel = cmbSucursal.getValue();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ArbolB arbol = null;
        if (sel != null) {
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s != null) arbol = s.getArbolBFechas();
        }

        if (arbol == null || arbol.getRaiz() == null ||
                arbol.getRaiz().getClaves().isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font(14));
            gc.fillText("Árbol B vacío — carga productos primero", 400, 200);
            return;
        }

        // Calcular altura y cantidad total de nodos para escalar
        int[] conteo = {0};
        int   alt    = calcularAltura(arbol.getRaiz(), conteo);
        int   total  = conteo[0];

        // Escala: si hay muchos nodos, reducir ancho de celda
        double escala  = Math.max(0.35, Math.min(1.0, 80.0 / Math.max(total, 1)));
        int nodoW = (int)(NODO_W_BASE * escala);
        int nodoH = (int)(NODO_H_BASE * escala);
        int sepV  = (int)(SEP_V_BASE  * Math.max(0.6, escala));

        // Ancho necesario: estimamos 2^alt hojas, cada una con ~t claves
        double anchoNec = Math.pow(2, alt) * (nodoW * 3 + 10);
        double altoNec  = (alt + 1) * (sepV + nodoH) + 60;

        double cW = Math.max(3000, anchoNec);
        double cH = Math.max(600,  altoNec);
        canvas.setWidth(cW);
        canvas.setHeight(cH);

        gc.clearRect(0, 0, cW, cH);
        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, cW, cH);

        lblInfo.setText(String.format(
                "Árbol B  |  Nodos: %d  |  Altura: %d  |  O(log n) ≈ %.1f",
                total, alt, Math.log(Math.max(total, 1)) / Math.log(2)
        ));

        dibujarNodoB(gc, arbol.getRaiz(),
                cW / 2.0, 40,
                cW / 4.0, 0,
                nodoW, nodoH, sepV);
    }

    @SuppressWarnings("unchecked")
    private void dibujarNodoB(GraphicsContext gc, NodoB nodo,
                              double cx, double cy,
                              double offset, int nivel,
                              int nodoW, int nodoH, int sepV) {
        if (nodo == null) return;

        List   claves    = nodo.getClaves();
        int    numCl     = claves.size();
        double nodoAncho = numCl * nodoW;
        double nodoX     = cx - nodoAncho / 2.0;

        // Color según nivel
        Color colorFondo = nodo.isEsHoja()
                ? Color.web("#27ae60")
                : Color.web("#8e44ad");

        // Sombra
        gc.setFill(Color.web("#00000015"));
        gc.fillRoundRect(nodoX + 2, cy + 2, nodoAncho, nodoH, 6, 6);

        gc.setFill(colorFondo);
        gc.fillRoundRect(nodoX, cy, nodoAncho, nodoH, 6, 6);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(nodoX, cy, nodoAncho, nodoH, 6, 6);

        double fontSize = Math.max(7, nodoH * 0.32);
        gc.setFont(Font.font(fontSize));

        for (int i = 0; i < numCl; i++) {
            if (i > 0) {
                gc.setStroke(Color.web("#ffffff88"));
                gc.setLineWidth(0.6);
                gc.strokeLine(nodoX + i * nodoW, cy,
                        nodoX + i * nodoW, cy + nodoH);
            }
            String clave = claves.get(i).toString();
            int maxLen   = Math.max(3, nodoW / 8);
            if (clave.length() > maxLen) clave = clave.substring(0, maxLen - 1) + ".";

            gc.setFill(Color.WHITE);
            gc.fillText(clave, nodoX + i * nodoW + 4, cy + nodoH / 2.0 + fontSize * 0.35);
        }

        if (!nodo.isEsHoja()) {
            List   hijos    = nodo.getHijos();
            int    numHijos = hijos.size();
            double paso     = (offset * 2) / Math.max(numHijos - 1, 1);
            double inicioX  = cx - offset;

            for (int i = 0; i < numHijos; i++) {
                double hijoX = numHijos == 1 ? cx : inicioX + i * paso;
                double hijoY = cy + sepV + nodoH;

                gc.setStroke(Color.web("#bdc3c7"));
                gc.setLineWidth(1.0);
                gc.strokeLine(cx, cy + nodoH, hijoX, hijoY);

                dibujarNodoB(gc, (NodoB) hijos.get(i),
                        hijoX, hijoY, offset / 2, nivel + 1,
                        nodoW, nodoH, sepV);
            }
        }
    }

    /** Devuelve la altura y suma los nodos en conteo[0] */
    @SuppressWarnings("unchecked")
    private int calcularAltura(NodoB nodo, int[] conteo) {
        if (nodo == null) return 0;
        conteo[0]++;
        if (nodo.isEsHoja()) return 1;
        int maxH = 0;
        for (Object h : nodo.getHijos()) {
            maxH = Math.max(maxH, calcularAltura((NodoB) h, conteo));
        }
        return maxH + 1;
    }

    private HBox crearLeyenda() {
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(5));
        String[][] items = {
                {"Nodo interno", "#8e44ad"},
                {"Nodo hoja",    "#27ae60"}
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
}