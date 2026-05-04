package ui.estructuras_view;

import clases.Sucursal;
import estructuras.hash.NodoTabla;
import estructuras.hash.TablaHash;
import estructuras.lista.ListaEnlazada;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.view.AppState;
import ui.view.PanelExportacion;
import utils.ExportarEstructuras;

public class HashView extends VBox {

    private AppState state;
    private Canvas canvas;
    private ComboBox<String> cmbSucursal;
    private Label lblFactor;
    private Tooltip tooltip;

    // ── Dimensiones base ──────────────────────────────────────────────────────
    private static final int BUCKET_H = 42;   // altura del bloque-bucket
    private static final int BUCKET_W = 180;  // ancho del bloque-bucket
    private static final int NODE_H = 34;   // altura de cada nodo encadenado
    private static final int NODE_W = 176;  // ancho de cada nodo (código completo)
    private static final int ARROW_W = 24;   // ancho de la flecha entre nodos
    private static final int NULL_W = 32;   // ancho del bloque "null"
    private static final int PAD_X = 18;
    private static final int PAD_Y = 18;
    private static final int GAP_Y = 8;    // espacio vertical entre buckets
    private static final int NODE_GAP = 4;    // espacio horizontal entre nodos

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color C_EMPTY_FILL = Color.web("#f1f5f9");
    private static final Color C_EMPTY_TEXT = Color.web("#94a3b8");
    private static final Color C_SINGLE = Color.web("#22c55e");
    private static final Color C_WARN = Color.web("#f59e0b");
    private static final Color C_DANGER = Color.web("#ef4444");
    private static final Color C_NODE_EXTRA = Color.web("#f97316"); // nodos 2,3,4…
    private static final Color C_ARROW = Color.web("#94a3b8");
    private static final Color C_NULL_FILL = Color.web("#e2e8f0");
    private static final Color C_NULL_TEXT = Color.web("#64748b");
    private static final Color C_BG = Color.web("#f8fafc");
    private static final Color C_FOOTER_TEXT = Color.web("#64748b");
    private static final Color C_BADGE_WARN = Color.web("#b45309");

    public HashView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Visualización Tabla Hash ");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("Seleccionar sucursal");
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmbSucursal.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }

        Button btnDibujar = new Button("Dibujar tabla");
        btnDibujar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 6 16; -fx-background-radius: 6;");

        CheckBox chkVacios = new CheckBox("Mostrar buckets vacíos");

        lblFactor = new Label("Factor de carga: —");
        lblFactor.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        HBox controles = new HBox(10, new Label("Sucursal:"), cmbSucursal, btnDibujar, chkVacios, lblFactor);
        controles.setStyle("-fx-alignment: center-left;");

        HBox leyenda = crearLeyenda();

        Label lblInfo = new Label("Resolución de colisiones por encadenamiento colocarse sobre un nodo para ver detalles");
        lblInfo.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        canvas = new Canvas(1200, 600);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(660);
        scroll.setFitToWidth(false);
        scroll.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        tooltip = new Tooltip();
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 12;");
        canvas.setOnMouseMoved(e -> mostrarTooltip(e.getX(), e.getY(), e.getScreenX(), e.getScreenY()));
        canvas.setOnMouseExited(e -> tooltip.hide());

        btnDibujar.setOnAction(e -> dibujar(chkVacios.isSelected()));
        chkVacios.setOnAction(e -> dibujar(chkVacios.isSelected()));

        this.getChildren().addAll(titulo, new Separator(), controles, lblInfo, leyenda, scroll, panelExport);
    }

    private record HitArea(double x, double y, double w, double h, int bucket, int nodoIdx, String clave, int totalEnBucket) {
    }

    private final ListaEnlazada<HitArea> hitAreas = new ListaEnlazada<>();

    private void dibujar(boolean mostrarVacios) {
        hitAreas.clear();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String sel = cmbSucursal.getValue();

        TablaHash tabla = null;
        if (sel != null) {
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s != null) tabla = s.getTablaHash();
        }

        if (tabla == null || tabla.isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.web("#64748b"));
            gc.setFont(Font.font("System", 14));
            gc.fillText("Tabla Hash vacía — carga productos primero.", 40, 80);
            return;
        }

        int capacidad = tabla.getCapacidad();
        int elementos = tabla.size();
        double factorCarga = tabla.factorCarga();
        lblFactor.setText(String.format("Factor de carga: %.3f  (%d elem. / %d buckets)", factorCarga, elementos, capacidad));

        int filasMostradas = 0;
        int maxCadena = 0;
        for (int i = 0; i < capacidad; i++) {
            int tam = tabla.tamanioBucket(i);
            if (tam > 0 || mostrarVacios) filasMostradas++;
            if (tam > maxCadena) maxCadena = tam;
        }

        double canvasW = PAD_X * 2 + BUCKET_W + ARROW_W + (long) maxCadena * (NODE_W + NODE_GAP + ARROW_W) + NULL_W + 20;
        double canvasH = PAD_Y + (long) filasMostradas * (BUCKET_H + GAP_Y) + PAD_Y + 24;

        canvas.setWidth(Math.max(1200, canvasW));
        canvas.setHeight(Math.max(500, canvasH));

        gc.setFill(C_BG);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int fila = 0;
        int vacios = 0;
        int colisiones = 0;

        for (int i = 0; i < capacidad; i++) {
            int tam = tabla.tamanioBucket(i);
            if (tam == 0) {
                vacios++;
                if (!mostrarVacios) continue;
            }
            if (tam > 1) colisiones++;

            double y = PAD_Y + (long) fila * (BUCKET_H + GAP_Y);
            double x = PAD_X;

            dibujarBucket(gc, x, y, i, tam);

            if (tam > 0) {
                NodoTabla nodo = tabla.getBuckets()[i];
                int k = 0;
                double nodeX = x + BUCKET_W + ARROW_W;

                while (nodo != null) {
                    double fromX = (k == 0) ? x + BUCKET_W : nodeX - ARROW_W;
                    double arrowY = y + BUCKET_H / 2.0;
                    dibujarFlecha(gc, fromX, arrowY, nodeX, arrowY);

                    Color colorNodo = (k == 0 && tam == 1) ? C_SINGLE : (k == 0) ? C_WARN : C_NODE_EXTRA;
                    double nodeY = y + (BUCKET_H - NODE_H) / 2.0;
                    dibujarNodo(gc, nodeX, nodeY, NODE_W, NODE_H, colorNodo, k, nodo.getClave());

                    hitAreas.agregar(new HitArea(nodeX, nodeY, NODE_W, NODE_H, i, k, nodo.getClave(), tam));

                    if (nodo.getSiguiente() == null) {
                        double nullFromX = nodeX + NODE_W;
                        dibujarFlecha(gc, nullFromX, arrowY, nullFromX + ARROW_W, arrowY);
                        dibujarNull(gc, nullFromX + ARROW_W, nodeY);
                    }

                    nodo = nodo.getSiguiente();
                    nodeX += NODE_W + NODE_GAP + ARROW_W;
                    k++;
                }
            }
            fila++;
        }

        gc.setFill(C_FOOTER_TEXT);
        gc.setFont(Font.font("System", 11));
        double footY = PAD_Y + (long) fila * (BUCKET_H + GAP_Y) + 16;
        gc.fillText(String.format("Total: %d buckets  ·  Ocupados: %d  ·  Vacíos: %d  ·  Buckets con colisión: %d", capacidad, capacidad - vacios, vacios, colisiones), PAD_X, footY);
    }

    private void dibujarBucket(GraphicsContext gc, double x, double y, int idx, int tam) {
        Color fill = tam == 0 ? C_EMPTY_FILL : tam == 1 ? C_SINGLE : tam <= 3 ? C_WARN : C_DANGER;

        gc.setFill(fill);
        gc.fillRoundRect(x, y, BUCKET_W, BUCKET_H, 8, 8);
        gc.setStroke(tam == 0 ? Color.web("#cbd5e1") : fill.darker());
        gc.setLineWidth(0.8);
        gc.strokeRoundRect(x, y, BUCKET_W, BUCKET_H, 8, 8);

        gc.setFill(Color.color(0, 0, 0, 0.18));
        gc.fillRoundRect(x + 5, y + 7, 30, BUCKET_H - 14, 5, 5);
        gc.setFill(tam == 0 ? C_EMPTY_TEXT : Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(String.valueOf(idx), x + 20, y + BUCKET_H / 2.0);

        if (tam == 0) {
            gc.setFill(C_EMPTY_TEXT);
            gc.setFont(Font.font("System", 12));
            gc.fillText("vacío", x + 44, y + BUCKET_H / 2.0);
        } else {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            String label = tam == 1 ? "1 elemento" : tam + " elementos";
            gc.fillText(label, x + 44, y + BUCKET_H / 2.0);

            if (tam > 1) {
                double bx = x + BUCKET_W - 80;
                gc.setFill(Color.color(0, 0, 0, 0.18));
                gc.fillRoundRect(bx, y + 8, 74, BUCKET_H - 16, 5, 5);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("System", FontWeight.BOLD, 10));
                gc.fillText("colisión", bx + 8, y + BUCKET_H / 2.0);
            }
        }
    }

    private void dibujarNodo(GraphicsContext gc, double x, double y, double w, double h, Color color, int idx, String clave) {
        gc.setFill(color);
        gc.fillRoundRect(x, y, w, h, 5, 5);
        gc.setStroke(color.brighter());
        gc.setLineWidth(0.8);
        gc.strokeRoundRect(x, y, w, h, 5, 5);

        gc.setFill(Color.color(0, 0, 0, 0.2));
        gc.fillRoundRect(x + 3, y + 4, 17, h - 8, 3, 3);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 9));
        gc.fillText(String.valueOf(idx), x + 7, y + h / 2.0);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 11));
        gc.fillText(clave, x + 24, y + h / 2.0);
    }


    private void dibujarNull(GraphicsContext gc, double x, double y) {
        gc.setFill(C_NULL_FILL);
        gc.fillRoundRect(x, y + (BUCKET_H - NODE_H) / 2.0 + 4, NULL_W, NODE_H - 8, 4, 4);
        gc.setFill(C_NULL_TEXT);
        gc.setFont(Font.font("System", FontWeight.BOLD, 9));
        gc.fillText("null", x + 4, y + (BUCKET_H - NODE_H) / 2.0 + NODE_H / 2.0);
    }

    private void dibujarFlecha(GraphicsContext gc, double x1, double y, double x2, double yEnd) {
        gc.setStroke(C_ARROW);
        gc.setLineWidth(1.5);
        gc.strokeLine(x1, y, x2 - 7, y);
        // Punta de flecha
        gc.setFill(C_ARROW);
        gc.fillPolygon(new double[]{x2, x2 - 8, x2 - 8}, new double[]{y, y - 4, y + 4}, 3);
    }

    private void mostrarTooltip(double mx, double my, double screenX, double screenY) {
        for (HitArea h : hitAreas) {
            if (mx >= h.x() && mx <= h.x() + h.w() && my >= h.y() && my <= h.y() + h.h()) {
                String warn = h.totalEnBucket() > 1 ? "Colisión — " + h.totalEnBucket() + " elementos en bucket " + h.bucket() : "Sin colisión";
                tooltip.setText("Bucket: " + h.bucket() + "\n" + "Nodo:   #" + h.nodoIdx() + (h.nodoIdx() == 0 && h.totalEnBucket() > 1 ? " (primero en cadena)" : "") + "\n" + "Código: " + h.clave() + "\n" + warn);
                tooltip.show(canvas, screenX + 14, screenY - 10);
                return;
            }
        }
        tooltip.hide();
    }


    private HBox crearLeyenda() {
        HBox hbox = new HBox(16);
        hbox.setPadding(new Insets(6, 0, 6, 0));
        hbox.setStyle("-fx-alignment: center-left;");

        String[][] items = {{"Sin colisión (1 elem)", "#22c55e"}, {"Colisión leve (2–3 elem)", "#f59e0b"}, {"Colisión grave (4+ elem)", "#ef4444"}, {"Nodo encadenado extra", "#f97316"}, {"Vacío", "#e2e8f0"},};
        for (String[] item : items) {
            Canvas dot = new Canvas(13, 13);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(item[1]));
            g.fillRoundRect(0, 0, 13, 13, 4, 4);
            Label l = new Label(item[0]);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
            hbox.getChildren().addAll(dot, l);
        }
        return hbox;
    }

    PanelExportacion panelExport = new PanelExportacion("Tabla Hash", () -> {
        String sel = cmbSucursal.getValue();
        if (sel == null) return "";
        int id = Integer.parseInt(sel.split(" - ")[0]);
        Sucursal s = state.getCargaCSV().buscarSucursal(id);
        if (s == null) return "";
        return ExportarEstructuras.hashToDot(s.getTablaHash(), "Hash_S" + id);
    }, (ruta, fmt) -> {
        String sel = cmbSucursal.getValue();
        if (sel == null) return false;
        int id = Integer.parseInt(sel.split(" - ")[0]);
        Sucursal s = state.getCargaCSV().buscarSucursal(id);
        if (s == null) return false;
        return ExportarEstructuras.exportarHashImagen(s.getTablaHash(), ruta, fmt);
    });
}