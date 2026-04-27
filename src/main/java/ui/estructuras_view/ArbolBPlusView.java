package ui.estructuras_view;

import clases.Sucursal;
import estructuras.arbolBPlus.ArbolBPlus;
import estructuras.arbolBPlus.NodoBPlus;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ui.view.AppState;

import java.util.List;

public class ArbolBPlusView extends VBox {

    private AppState state;
    private Canvas canvas;
    private ComboBox<String> cmbSucursal;
    private Label lblInfo;

    private static final int NODO_H_BASE = 34;
    private static final int NODO_W_BASE = 110;
    private static final int SEP_V_BASE  = 85;

    public ArbolBPlusView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Visualización — Árbol B+");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("Seleccionar sucursal");
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmbSucursal.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }

        Button btnDibujar = new Button("Dibujar Árbol B+");
        btnDibujar.setStyle("-fx-background-color: #d35400; -fx-text-fill: white; -fx-padding: 6 14;");

        lblInfo = new Label("Árbol B+ indexado por categoría  |  Las hojas están enlazadas →");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d;");

        HBox controles = new HBox(10,
                new Label("Sucursal:"), cmbSucursal, btnDibujar, lblInfo
        );

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

        ArbolBPlus arbol = null;
        if (sel != null) {
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s != null) arbol = s.getArbolBPlusCategoria();
        }

        if (arbol == null || arbol.getRaiz() == null ||
                arbol.getRaiz().getClaves().isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font(14));
            gc.fillText("Árbol B+ vacío — carga productos primero", 400, 200);
            return;
        }

        // Calcular métricas del árbol para escalar
        int[] conteo   = {0, 0}; // [0]=nodos totales, [1]=hojas
        int   alt      = calcularInfo(arbol.getRaiz(), conteo);
        int   totalNod = conteo[0];
        int   hojas    = conteo[1];

        double escala = Math.max(0.3, Math.min(1.0, 60.0 / Math.max(totalNod, 1)));
        int nodoW = (int)(NODO_W_BASE * escala);
        int nodoH = (int)(NODO_H_BASE * escala);
        int sepV  = (int)(SEP_V_BASE  * Math.max(0.55, escala));

        double anchoNec = Math.pow(2, alt) * (nodoW * 3 + 10);
        double altoNec  = (alt + 1) * (sepV + nodoH) + 100;

        double cW = Math.max(3000, anchoNec);
        double cH = Math.max(650,  altoNec);
        canvas.setWidth(cW);
        canvas.setHeight(cH);

        gc.clearRect(0, 0, cW, cH);
        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, cW, cH);

        lblInfo.setText(String.format(
                "B+  |  Nodos: %d  |  Hojas enlazadas: %d  |  Altura: %d",
                totalNod, hojas, alt
        ));

        // Arrays para guardar posición de hojas (para dibujar enlaces)
        // Usamos listas dinámicas en lugar de arrays fijos de [100]
        java.util.ArrayList<Double> posHojasX = new java.util.ArrayList<>();
        java.util.ArrayList<Double> posHojasY = new java.util.ArrayList<>();

        dibujarNodoBPlus(gc, arbol.getRaiz(),
                cW / 2.0, 40, cW / 4.0,
                posHojasX, posHojasY,
                nodoW, nodoH, sepV);

        dibujarEnlacesHojas(gc, posHojasX, posHojasY, nodoW);
    }

    @SuppressWarnings("unchecked")
    private void dibujarNodoBPlus(GraphicsContext gc, NodoBPlus nodo,
                                  double cx, double cy, double offset,
                                  java.util.ArrayList<Double> posX,
                                  java.util.ArrayList<Double> posY,
                                  int nodoW, int nodoH, int sepV) {
        if (nodo == null) return;

        List   claves    = nodo.getClaves();
        int    numCl     = claves.size();
        double nodoAncho = numCl * nodoW;
        double nodoX     = cx - nodoAncho / 2.0;

        Color colorFondo = nodo.isEsHoja()
                ? Color.web("#d35400")
                : Color.web("#2c3e50");

        // Sombra
        gc.setFill(Color.web("#00000015"));
        gc.fillRoundRect(nodoX + 2, cy + 2, nodoAncho, nodoH, 6, 6);

        gc.setFill(colorFondo);
        gc.fillRoundRect(nodoX, cy, nodoAncho, nodoH, 6, 6);
        gc.setStroke(nodo.isEsHoja() ? Color.web("#e67e22") : Color.web("#7f8c8d"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(nodoX, cy, nodoAncho, nodoH, 6, 6);

        double fontSize = Math.max(7, nodoH * 0.32);
        gc.setFont(Font.font(fontSize));

        for (int i = 0; i < numCl; i++) {
            if (i > 0) {
                gc.setStroke(Color.web("#95a5a6"));
                gc.setLineWidth(0.6);
                gc.strokeLine(nodoX + i * nodoW, cy,
                        nodoX + i * nodoW, cy + nodoH);
            }
            String clave  = claves.get(i).toString();
            int    maxLen = Math.max(3, nodoW / 8);
            if (clave.length() > maxLen) clave = clave.substring(0, maxLen - 1) + ".";

            gc.setFill(Color.WHITE);
            gc.fillText(clave, nodoX + i * nodoW + 4, cy + nodoH / 2.0 + fontSize * 0.35);
        }

        if (nodo.isEsHoja()) {
            // Guardar posición del lado derecho para el enlace
            posX.add(nodoX + nodoAncho);
            posY.add(cy + nodoH / 2.0);
        }

        if (!nodo.isEsHoja()) {
            List   hijos    = nodo.getHijos();
            int    numHijos = hijos.size();
            if (numHijos == 0) return;

            double paso    = (offset * 2) / Math.max(numHijos - 1, 1);
            double inicioX = cx - offset;

            for (int i = 0; i < numHijos; i++) {
                double hijoX = numHijos == 1 ? cx : inicioX + i * paso;
                double hijoY = cy + sepV + nodoH;

                gc.setStroke(Color.web("#bdc3c7"));
                gc.setLineWidth(1.0);
                gc.strokeLine(cx, cy + nodoH, hijoX, hijoY);

                dibujarNodoBPlus(gc, (NodoBPlus) hijos.get(i),
                        hijoX, hijoY, offset / 2,
                        posX, posY, nodoW, nodoH, sepV);
            }
        }
    }

    private void dibujarEnlacesHojas(GraphicsContext gc,
                                     java.util.ArrayList<Double> posX,
                                     java.util.ArrayList<Double> posY,
                                     int nodoW) {
        int total = posX.size();
        gc.setStroke(Color.web("#f39c12"));
        gc.setLineWidth(2);

        for (int i = 0; i < total - 1; i++) {
            double x1 = posX.get(i);
            double y1 = posY.get(i);
            double x2 = posX.get(i + 1) - nodoW * 0.5;
            double y2 = posY.get(i + 1);

            gc.strokeLine(x1, y1, x2, y2);

            // Flecha
            gc.setFill(Color.web("#f39c12"));
            double[] fx = {x2, x2 - 7, x2 - 7};
            double[] fy = {y2, y2 - 4, y2 + 4};
            gc.fillPolygon(fx, fy, 3);
        }
    }

    @SuppressWarnings("unchecked")
    private int calcularInfo(NodoBPlus nodo, int[] conteo) {
        if (nodo == null) return 0;
        conteo[0]++;
        if (nodo.isEsHoja()) { conteo[1]++; return 1; }
        int maxH = 0;
        for (Object h : nodo.getHijos()) {
            maxH = Math.max(maxH, calcularInfo((NodoBPlus) h, conteo));
        }
        return maxH + 1;
    }

    private HBox crearLeyenda() {
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(5));
        String[][] items = {
                {"Nodo interno", "#2c3e50"},
                {"Nodo hoja",    "#d35400"},
                {"Enlace hojas", "#f39c12"}
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