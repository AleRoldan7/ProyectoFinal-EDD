package ui.estructuras_view;

import clases.Sucursal;
import estructuras.arbolB.ArbolB;
import estructuras.arbolB.NodoB;
import estructuras.lista.ListaEnlazada;
import estructuras.nodo.Nodo;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ui.view.AppState;
import ui.view.PanelExportacion;
import utils.ExportarEstructuras;

import java.awt.image.BufferedImage;

public class ArbolBView extends VBox {

    private AppState state;
    private Canvas canvas;
    private ComboBox<String> cmbSucursal;
    private Label lblInfo;

    // ── Constantes de layout ─────────────────────────────────────────────────
    private static final int CELDA_W = 88;
    private static final int NODO_H = 36;
    private static final int SEP_V = 72;
    private static final int SEP_H = 18;
    private static final int PAD_TOP = 56;

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
        btnDibujar.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;" + "-fx-font-size: 13px; -fx-padding: 7 18; -fx-background-radius: 6;");

        lblInfo = new Label("Árbol B — índice por fecha de caducidad  |  O(log n) búsqueda por rango");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        HBox controles = new HBox(12, new Label("Sucursal:"), cmbSucursal, btnDibujar, lblInfo);
        controles.setStyle("-fx-alignment: center-left;");

        canvas = new Canvas(2400, 700);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(680);
        scroll.setFitToWidth(false);
        scroll.setStyle("-fx-background: #fafafa; -fx-border-color: #dcdde1; -fx-border-radius: 6;");

        btnDibujar.setOnAction(e -> dibujar());

        this.getChildren().addAll(titulo, new Separator(), controles, crearLeyenda(), scroll, panelExport);
        dibujarVacio("Selecciona una sucursal y pulsa «Dibujar Árbol B»");
    }

    @SuppressWarnings("unchecked")
    private void dibujar() {
        String sel = cmbSucursal.getValue();
        if (sel == null) {
            dibujarVacio("Selecciona una sucursal primero");
            return;
        }

        int id = Integer.parseInt(sel.split(" - ")[0].trim());
        Sucursal s = state.getCargaCSV().buscarSucursal(id);
        ArbolB arbol = (s != null) ? s.getArbolBFechas() : null;

        if (arbol == null || arbol.getRaiz() == null || arbol.getRaiz().getNumClaves() == 0) {
            dibujarVacio("Árbol B vacío — carga productos primero: " + sel);
            return;
        }

        NodoB raiz = arbol.getRaiz();

        asignarAnchos(raiz);

        ListaEnlazada<NodoPosicion> posiciones = new ListaEnlazada<>();
        double canvasW = Math.max(2400, raiz.anchoSubarbol + 80);
        asignarPosiciones(raiz, (canvasW - raiz.anchoSubarbol) / 2.0, PAD_TOP, posiciones);

        double maxY = maxYDeLista(posiciones);
        double canvasH = Math.max(600, maxY + 40);

        canvas.setWidth(canvasW);
        canvas.setHeight(canvasH);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);
        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, canvasW, canvasH);

        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Árbol B — Sucursal: " + sel, 16, 28);

        dibujarAristas(gc, raiz);

        Nodo<NodoPosicion> cur = posiciones.getHead();
        while (cur != null) {
            dibujarNodoBFX(gc, cur.getProducto().nodo, cur.getProducto().x, cur.getProducto().y);
            cur = cur.getNext();
        }

        int[] cnt = {0};
        int alt = calcularAltura(raiz, cnt);
        lblInfo.setText(String.format("Árbol B  |  Sucursal %d  |  Nodos: %d  |  Altura: %d  |  O(log n)", id, cnt[0], alt));
    }

    private void asignarAnchos(NodoB nodo) {
        if (nodo == null) return;
        int numCl = nodo.getNumClaves();

        if (nodo.isEsHoja()) {
            nodo.anchoSubarbol = numCl * CELDA_W + SEP_H * 2;
            return;
        }

        double sumaHijos = 0;
        for (int i = 0; i <= numCl; i++) {
            NodoB hijo = nodo.getHijos()[i];
            if (hijo != null) {
                asignarAnchos(hijo);
                sumaHijos += hijo.anchoSubarbol;
            } else {
                sumaHijos += CELDA_W + SEP_H * 2;
            }
        }
        sumaHijos += SEP_H * numCl;   // separación entre hijos

        nodo.anchoSubarbol = Math.max(numCl * CELDA_W + SEP_H * 2, sumaHijos);
    }

    private void asignarPosiciones(NodoB nodo, double inicioX, double y, ListaEnlazada<NodoPosicion> lista) {
        if (nodo == null) return;

        int numCl = nodo.getNumClaves();
        double cx = inicioX + nodo.anchoSubarbol / 2.0;
        double nx = cx - (numCl * CELDA_W) / 2.0;

        lista.agregar(new NodoPosicion(nodo, nx, y));
        nodo.posX = nx;
        nodo.posY = y;

        if (!nodo.isEsHoja()) {
            double hijoY = y + NODO_H + SEP_V;
            double cursor = inicioX;
            for (int i = 0; i <= numCl; i++) {
                NodoB hijo = nodo.getHijos()[i];
                if (hijo != null) {
                    asignarPosiciones(hijo, cursor, hijoY, lista);
                    cursor += hijo.anchoSubarbol + SEP_H;
                } else {
                    cursor += CELDA_W + SEP_H * 2 + SEP_H;
                }
            }
        }
    }


    private double maxYDeLista(ListaEnlazada<NodoPosicion> lista) {
        double max = 600;
        Nodo<NodoPosicion> cur = lista.getHead();
        while (cur != null) {
            double val = cur.getProducto().y + NODO_H + 10;
            if (val > max) max = val;
            cur = cur.getNext();
        }
        return max;
    }

    private void dibujarAristas(GraphicsContext gc, NodoB nodo) {
        if (nodo == null || nodo.isEsHoja()) return;

        int numCl = nodo.getNumClaves();
        double cx = nodo.posX + (numCl * CELDA_W) / 2.0;

        for (int i = 0; i <= numCl; i++) {
            NodoB hijo = nodo.getHijos()[i];
            if (hijo == null) continue;

            double hijoCX = hijo.posX + (hijo.getNumClaves() * CELDA_W) / 2.0;

            gc.setStroke(Color.web("#95a5a6"));
            gc.setLineWidth(1.5);
            gc.strokeLine(cx, nodo.posY + NODO_H, hijoCX, hijo.posY);

            dibujarAristas(gc, hijo);
        }
    }

    private void dibujarNodoBFX(GraphicsContext gc, NodoB nodo, double nx, double ny) {
        int numCl = nodo.getNumClaves();
        double totalW = numCl * CELDA_W;

        gc.setFill(Color.web("#00000018"));
        gc.fillRoundRect(nx + 3, ny + 3, totalW, NODO_H, 8, 8);

        gc.setFill(nodo.isEsHoja() ? Color.web("#27ae60") : Color.web("#8e44ad"));
        gc.fillRoundRect(nx, ny, totalW, NODO_H, 8, 8);

        gc.setStroke(Color.web("#ffffff55"));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(nx, ny, totalW, NODO_H, 8, 8);

        Font fontClave = Font.font("Arial", FontWeight.BOLD, 10);

        for (int i = 0; i < numCl; i++) {
            double celdaX = nx + i * CELDA_W;

            if (i > 0) {
                gc.setStroke(Color.web("#ffffff55"));
                gc.setLineWidth(0.8);
                gc.strokeLine(celdaX, ny + 4, celdaX, ny + NODO_H - 4);
            }

            String clave = (nodo.getClaves()[i] != null) ? nodo.getClaves()[i].toString() : "?";
            clave = formatearClave(clave);

            gc.setFill(Color.WHITE);
            gc.setFont(fontClave);
            double tw = medirTexto(clave, fontClave);
            gc.fillText(clave, celdaX + (CELDA_W - tw) / 2.0, ny + NODO_H / 2.0 + 4.0);
        }

        gc.setFont(Font.font("Arial", 7));
        gc.setFill(Color.web("#ffffff99"));
        gc.fillText(nodo.isEsHoja() ? "hoja" : "int", nx + 3, ny + NODO_H - 3);
    }

    @SuppressWarnings("unchecked")
    private boolean exportarBImagen(ArbolB<String> arbol, String ruta, String formato) {
        if (arbol == null || arbol.isEmpty()) return false;

        NodoB raiz = arbol.getRaiz();
        asignarAnchos(raiz);

        ListaEnlazada<NodoPosicion> posiciones = new ListaEnlazada<>();
        asignarPosiciones(raiz, 40, PAD_TOP, posiciones);

        double maxY = maxYDeLista(posiciones);
        int imgW = (int) Math.max(2400, raiz.anchoSubarbol + 80);
        int imgH = (int) Math.max(600, maxY + 60);

        try {
            BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(java.awt.Color.decode("#fafafa"));
            g2.fillRect(0, 0, imgW, imgH);
            g2.setColor(java.awt.Color.decode("#2c3e50"));
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            int[] cnt = {0};
            g2.drawString("Árbol B — " + cnt[0] + " nodos — Altura: " + calcularAltura(raiz, cnt), 16, 28);

            dibujarAristasSwing(g2, raiz);

            Nodo<NodoPosicion> cur = posiciones.getHead();
            while (cur != null) {
                dibujarNodoBSwing(g2, cur.getProducto().nodo, cur.getProducto().x, cur.getProducto().y);
                cur = cur.getNext();
            }

            g2.dispose();
            javax.imageio.ImageIO.write(img, formato, new java.io.File(ruta));
            return true;
        } catch (Exception e) {
            System.err.println("Error exportando Árbol B: " + e.getMessage());
            return false;
        }
    }

    private void dibujarAristasSwing(java.awt.Graphics2D g2, NodoB nodo) {
        if (nodo == null || nodo.isEsHoja()) return;
        int numCl = nodo.getNumClaves();
        double cx = nodo.posX + (numCl * CELDA_W) / 2.0;

        for (int i = 0; i <= numCl; i++) {
            NodoB hijo = nodo.getHijos()[i];
            if (hijo == null) continue;
            double hijoCX = hijo.posX + (hijo.getNumClaves() * CELDA_W) / 2.0;
            g2.setColor(java.awt.Color.decode("#95a5a6"));
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawLine((int) cx, (int) (nodo.posY + NODO_H), (int) hijoCX, (int) hijo.posY);
            dibujarAristasSwing(g2, hijo);
        }
    }

    private void dibujarNodoBSwing(java.awt.Graphics2D g2, NodoB nodo, double nx, double ny) {
        int numCl = nodo.getNumClaves();
        int totalW = numCl * CELDA_W;

        g2.setColor(new java.awt.Color(0, 0, 0, 25));
        g2.fillRoundRect((int) nx + 3, (int) ny + 3, totalW, NODO_H, 8, 8);

        g2.setColor(nodo.isEsHoja() ? java.awt.Color.decode("#27ae60") : java.awt.Color.decode("#8e44ad"));
        g2.fillRoundRect((int) nx, (int) ny, totalW, NODO_H, 8, 8);

        g2.setColor(new java.awt.Color(255, 255, 255, 80));
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.drawRoundRect((int) nx, (int) ny, totalW, NODO_H, 8, 8);

        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
        java.awt.FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < numCl; i++) {
            int celdaX = (int) nx + i * CELDA_W;
            if (i > 0) {
                g2.setColor(new java.awt.Color(255, 255, 255, 80));
                g2.drawLine(celdaX, (int) ny + 4, celdaX, (int) ny + NODO_H - 4);
            }
            String clave = (nodo.getClaves()[i] != null) ? nodo.getClaves()[i].toString() : "?";
            if (clave.length() > 11) clave = clave.substring(0, 10) + "…";

            int tw = fm.stringWidth(clave);
            g2.setColor(java.awt.Color.WHITE);
            g2.drawString(clave, celdaX + (CELDA_W - tw) / 2, (int) ny + NODO_H / 2 + fm.getAscent() / 2 - 2);
        }
    }

    private String formatearClave(String clave) {
        if (clave == null) return "?";
        // Fecha ISO "YYYY-MM-DD" — 10 chars, entra completa en CELDA_W=88px
        if (clave.length() == 10 && clave.charAt(4) == '-' && clave.charAt(7) == '-') {
            return clave;
        }
        if (clave.length() > 11) return clave.substring(0, 10) + "…";
        return clave;
    }

    private double medirTexto(String texto, Font font) {
        Text t = new Text(texto);
        t.setFont(font);
        return t.getLayoutBounds().getWidth();
    }

    private void dibujarVacio(String mensaje) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#95a5a6"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.fillText(mensaje, 40, canvas.getHeight() / 2);
    }

    private int calcularAltura(NodoB nodo, int[] conteo) {
        if (nodo == null) return 0;
        conteo[0]++;
        if (nodo.isEsHoja()) return 1;
        int maxH = 0;
        for (Object h : nodo.getHijos()) {
            if (h != null) maxH = Math.max(maxH, calcularAltura((NodoB) h, conteo));
        }
        return maxH + 1;
    }


    private HBox crearLeyenda() {
        HBox hbox = new HBox(18);
        hbox.setPadding(new Insets(4, 0, 4, 0));
        String[][] items = {{"Nodo interno", "#8e44ad"}, {"Nodo hoja", "#27ae60"}};
        for (String[] item : items) {
            Canvas c = new Canvas(14, 14);
            c.getGraphicsContext2D().setFill(Color.web(item[1]));
            c.getGraphicsContext2D().fillRoundRect(0, 0, 14, 14, 4, 4);
            Label l = new Label(item[0]);
            l.setStyle("-fx-font-size: 11px;");
            hbox.getChildren().addAll(c, l);
        }
        Label lInfo = new Label("Cada celda = 1 clave (fecha YYYY-MM-DD)  |  88 px/clave");
        lInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
        hbox.getChildren().add(lInfo);
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

    private static class NodoPosicion {
        final NodoB nodo;
        final double x, y;

        NodoPosicion(NodoB nodo, double x, double y) {
            this.nodo = nodo;
            this.x = x;
            this.y = y;
        }
    }
}