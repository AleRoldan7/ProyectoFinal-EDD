package ui.estructuras_view;

import clases.Sucursal;
import estructuras.arbolB.ArbolB;
import estructuras.arbolB.NodoB;
import estructuras.lista.ListaEnlazada;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ui.view.AppState;
import ui.view.PanelExportacion;
import utils.ExportarEstructuras;

import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.scene.text.Text;

public class ArbolBView extends VBox {

    private AppState state;
    private Canvas canvas;
    private ComboBox<String> cmbSucursal;
    private Label lblInfo;

    // Dimensiones base del nodo individual — se escalan si hay muchos nodos
    private static final int NODO_H_BASE = 34;
    private static final int NODO_W_BASE = 120;
    private static final int SEP_V_BASE = 80;

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

        HBox controles = new HBox(10, new Label("Sucursal:"), cmbSucursal, btnDibujar, lblInfo);

        // Canvas inicial grande; se redimensiona al dibujar
        canvas = new Canvas(3000, 700);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(680);
        scroll.setFitToWidth(false);

        HBox leyenda = crearLeyenda();

        btnDibujar.setOnAction(e -> dibujar());

        this.getChildren().addAll(titulo, new Separator(), controles, leyenda, scroll, panelExport);
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

        if (arbol == null || arbol.getRaiz() == null || arbol.getRaiz().getNumClaves() == 0) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font(14));
            gc.fillText("Árbol B vacío — carga productos primero", 400, 200);
            return;
        }

        // Calcular altura y cantidad total de nodos para escalar
        int[] conteo = {0};
        int alt = calcularAltura(arbol.getRaiz(), conteo);
        int total = conteo[0];

        // Escala: si hay muchos nodos, reducir ancho de celda
        double escala = Math.max(0.35, Math.min(1.0, 80.0 / Math.max(total, 1)));
        int nodoW = (int) (NODO_W_BASE * escala);
        int nodoH = (int) (NODO_H_BASE * escala);
        int sepV = (int) (SEP_V_BASE * Math.max(0.6, escala));

        // Ancho necesario: estimamos 2^alt hojas, cada una con ~t claves
        double anchoNec = Math.pow(2, alt) * (nodoW * 3 + 10);
        double altoNec = (alt + 1) * (sepV + nodoH) + 60;

        double cW = Math.max(3000, anchoNec);
        double cH = Math.max(600, altoNec);
        canvas.setWidth(cW);
        canvas.setHeight(cH);

        gc.clearRect(0, 0, cW, cH);
        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, cW, cH);

        lblInfo.setText(String.format("Árbol B  |  Nodos: %d  |  Altura: %d  |  O(log n) ≈ %.1f", total, alt, Math.log(Math.max(total, 1)) / Math.log(2)));

        double ancho = calcularAnchoSubarbol(arbol.getRaiz(), nodoW);
        dibujarNodoB(gc, arbol.getRaiz(), cW / 2.0, 40, ancho / 2, 0, nodoW, nodoH, sepV);


    }

    @SuppressWarnings("unchecked")
    private void dibujarNodoB(GraphicsContext gc, NodoB nodo, double cx, double cy, double offset, int nivel, int nodoW, int nodoH, int sepV) {

        if (nodo == null) return;

        int numCl = nodo.getNumClaves();
        Object[] claves = nodo.getClaves();

        double nodoAncho = numCl * nodoW;
        double nodoX = cx - nodoAncho / 2.0;

        Color colorFondo = nodo.isEsHoja() ? Color.web("#27ae60") : Color.web("#8e44ad");

        gc.setFill(Color.web("#00000015"));
        gc.fillRoundRect(nodoX + 2, cy + 2, nodoAncho, nodoH, 6, 6);

        gc.setFill(colorFondo);
        gc.fillRoundRect(nodoX, cy, nodoAncho, nodoH, 6, 6);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(nodoX, cy, nodoAncho, nodoH, 6, 6);

        double fontSize = Math.max(7, nodoH * 0.32);
        gc.setFont(Font.font(fontSize));

        for (int i = 0; i < numCl; i++) {

            if (i > 0) {
                gc.setStroke(Color.web("#ffffff88"));
                gc.strokeLine(nodoX + i * nodoW, cy, nodoX + i * nodoW, cy + nodoH);
            }

            String clave = claves[i].toString();

            if (clave.length() >= 10 && clave.contains("-")) {
                clave = clave.substring(8, 10) + "/" + clave.substring(5, 7);
            }

            int maxLen = Math.max(6, nodoW / 7);
            if (clave.length() > maxLen) {
                clave = clave.substring(0, maxLen - 1) + ".";
            }

            gc.setFill(Color.WHITE);
            Font font = Font.font(fontSize);
            gc.setFont(font);

            Text text = new Text(clave);
            text.setFont(font);
            double textWidth = text.getLayoutBounds().getWidth();

            gc.fillText(clave,
                    nodoX + i * nodoW + (nodoW - textWidth) / 2,
                    cy + nodoH / 2.0 + fontSize * 0.35
            );
        }

        if (!nodo.isEsHoja()) {

            NodoB[] hijos = nodo.getHijos();
            int numHijos = numCl + 1;

            double inicioX = cx - offset;

            for (int i = 0; i < numHijos; i++) {

                if (hijos[i] == null) continue;

                double anchoHijo = calcularAnchoSubarbol(hijos[i], nodoW);

                double hijoX = inicioX + anchoHijo / 2;
                double hijoY = cy + sepV + nodoH;

                gc.setStroke(Color.web("#bdc3c7"));
                gc.strokeLine(cx, cy + nodoH, hijoX, hijoY);

                dibujarNodoB(gc, hijos[i], hijoX, hijoY, anchoHijo / 2, nivel + 1, nodoW, nodoH, sepV);

                inicioX += anchoHijo;
            }
        }
    }

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
        String[][] items = {{"Nodo interno", "#8e44ad"}, {"Nodo hoja", "#27ae60"}};
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

    PanelExportacion panelExport = new PanelExportacion("Árbol B", () -> {
        String sel = cmbSucursal.getValue();
        if (sel == null) return "";
        int id = Integer.parseInt(sel.split(" - ")[0]);
        Sucursal s = state.getCargaCSV().buscarSucursal(id);
        if (s == null) return "";
        return ExportarEstructuras.arbolBToDot(s.getArbolBFechas(), "ArbolB_S" + id);
    }, (ruta, fmt) -> {
        String sel = cmbSucursal.getValue();
        if (sel == null) return false;
        int id = Integer.parseInt(sel.split(" - ")[0]);
        Sucursal s = state.getCargaCSV().buscarSucursal(id);
        if (s == null) return false;
        return exportarBImagen(s.getArbolBFechas(), ruta, fmt);
    });

    private boolean exportarBImagen(ArbolB<String> arbol, String ruta, String formato) {
        if (arbol.isEmpty()) return false;
        int[] c = {0};
        int alt = calcularAltura(arbol.getRaiz(), c);
        int nW = 120, nH = 30, sepV = 70;
        int imgW = (int) (Math.pow(2, alt) * (nW * 2 + 8));
        imgW = Math.min(imgW, 30000);
        int imgH = (alt + 1) * (sepV + nH) + 60;

        try {
            java.awt.image.BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(java.awt.Color.decode("#fafafa"));
            g2.fillRect(0, 0, imgW, imgH);
            g2.setColor(java.awt.Color.decode("#2c3e50"));
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
            g2.drawString("Árbol B — " + c[0] + " nodos", 10, 22);

            double ancho = calcularAnchoSubarbol(arbol.getRaiz(), nW);

            dibujarBSwing(g2, arbol.getRaiz(), imgW / 2.0, 40, ancho / 2 , nW, nH, sepV);
            g2.dispose();
            javax.imageio.ImageIO.write(img, formato, new java.io.File(ruta));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void dibujarBSwing(Graphics2D g2, NodoB nodo, double cx, double cy, double off, int nW, int nH, int sepV) {
        if (nodo == null) return;
        int num = nodo.getNumClaves();
        double ancho = num * nW;
        double nx = cx - ancho / 2.0;

        java.awt.Color color = nodo.isEsHoja() ? java.awt.Color.decode("#27ae60") : java.awt.Color.decode("#8e44ad");

        g2.setColor(color);
        g2.fillRoundRect((int) nx, (int) cy, (int) ancho, nH, 6, 6);
        g2.setColor(java.awt.Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.drawRoundRect((int) nx, (int) cy, (int) ancho, nH, 6, 6);

        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 9));
        for (int i = 0; i < num; i++) {
            if (i > 0) g2.drawLine((int) (nx + i * nW), (int) cy, (int) (nx + i * nW), (int) (cy + nH));
            String c = nodo.getClaves()[i] != null ? nodo.getClaves()[i].toString() : "";
            if (c.length() > 12) c = c.substring(0, 11) + ".";
            g2.setColor(java.awt.Color.WHITE);
            g2.drawString(c, (int) (nx + i * nW + 3), (int) (cy + nH / 2.0 + 4));
        }
        if (!nodo.isEsHoja()) {
            int numH = num + 1;
            double paso = (off * 2) / Math.max(numH - 1, 1);
            double iniX = cx - off;
            for (int i = 0; i <= num; i++) {
                if (nodo.getHijos()[i] == null) continue;
                double hx = numH == 1 ? cx : iniX + i * paso;
                double hy = cy + sepV + nH;
                g2.setColor(java.awt.Color.decode("#bdc3c7"));
                g2.drawLine((int) cx, (int) (cy + nH), (int) hx, (int) hy);
                dibujarBSwing(g2, nodo.getHijos()[i], hx, hy, off / 2, nW, nH, sepV);
            }
        }
    }

    private double calcularAnchoSubarbol(NodoB nodo, int nodoW) {
        if (nodo == null) return nodoW;

        if (nodo.isEsHoja()) {
            return Math.max(nodoW, nodo.getNumClaves() * nodoW);
        }

        double ancho = 0;
        NodoB[] hijos = nodo.getHijos();

        for (int i = 0; i <= nodo.getNumClaves(); i++) {
            ancho += calcularAnchoSubarbol(hijos[i], nodoW);
        }

        ancho += nodo.getNumClaves() * 40;

        return Math.max(ancho, nodo.getNumClaves() * nodoW);
    }
}