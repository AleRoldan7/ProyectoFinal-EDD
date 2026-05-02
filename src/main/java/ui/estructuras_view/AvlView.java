// ui/estructuras_view/AvlView.java
package ui.estructuras_view;

import clases.Productos;
import clases.Sucursal;
import estructuras.avl.ArbolAVL;
import estructuras.avl.NodoAVL;
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
import ui.view.AppState;
import ui.view.PanelExportacion;
import utils.ExportarEstructuras;

public class AvlView extends VBox {

    private AppState state;
    private Canvas canvas;
    private ComboBox<String> cmbSucursal;
    private Label lblInfo;
    private Label lblNivel;
    private ArbolAVL<Productos> avlActual;

    private int nivelActual = 0;
    private int nivelesMostrar = 4;

    private static final int MAX_CANVAS_W = 14000;
    private static final int MAX_CANVAS_H = 800;
    private static final int RADIO = 22;
    private static final int SEP_V = 75;

    public AvlView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Visualización Árbol AVL");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("AVL global (todos)");
        recargarSucursales();

        Button btnDibujar = new Button("Dibujar AVL");
        btnDibujar.setStyle("-fx-background-color: #27ae60;" + "-fx-text-fill: white; -fx-padding: 6 14;");

        Button btnInOrder = new Button("📋 Ver inOrder");
        btnInOrder.setStyle("-fx-background-color: #8e44ad;" + "-fx-text-fill: white; -fx-padding: 6 14;");

        Button btnSubir = new Button("Subir niveles");
        Button btnBajar = new Button("Bajar niveles");
        Button btnVerTodo = new Button("Vista comprimida");

        btnSubir.setStyle("-fx-background-color: #2980b9;" + "-fx-text-fill: white; -fx-padding: 5 12;");
        btnBajar.setStyle("-fx-background-color: #2980b9;" + "-fx-text-fill: white; -fx-padding: 5 12;");
        btnVerTodo.setStyle("-fx-background-color: #e67e22;" + "-fx-text-fill: white; -fx-padding: 5 12;");

        Spinner<Integer> spnNiveles = new Spinner<>(1, 8, 4);
        spnNiveles.setPrefWidth(70);
        spnNiveles.valueProperty().addListener((o, v, n) -> {
            nivelesMostrar = n;
            if (avlActual != null) dibujarNiveles(avlActual);
        });

        lblNivel = new Label("Nivel: 0-3");
        lblNivel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");

        lblInfo = new Label("🔴 raíz | 🟢 hoja | 🔵 nodo interno | " + "h = altura ");
        lblInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        HBox controles1 = new HBox(10, new Label("Sucursal:"), cmbSucursal, btnDibujar, btnInOrder);
        HBox controles2 = new HBox(10, new Label("Niveles a mostrar:"), spnNiveles, btnSubir, btnBajar, btnVerTodo, lblNivel);
        controles2.setStyle("-fx-alignment: center-left;");

        canvas = new Canvas(1200, MAX_CANVAS_H);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(MAX_CANVAS_H + 4);
        scroll.setFitToWidth(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        TextArea txtInOrder = new TextArea();
        txtInOrder.setEditable(false);
        txtInOrder.setPrefHeight(90);
        txtInOrder.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnDibujar.setOnAction(e -> {
            recargarSucursales();
            avlActual = obtenerAVL();
            if (avlActual != null) {
                nivelActual = 0;
                actualizarInfo(avlActual);
                dibujarNiveles(avlActual);
            }
        });

        btnSubir.setOnAction(e -> {
            if (avlActual == null) return;
            nivelActual = Math.max(0, nivelActual - nivelesMostrar);
            dibujarNiveles(avlActual);
        });

        btnBajar.setOnAction(e -> {
            if (avlActual == null) return;
            int maxNivel = avlActual.getHeight() - 1;
            if (nivelActual + nivelesMostrar <= maxNivel) {
                nivelActual += nivelesMostrar;
                dibujarNiveles(avlActual);
            }
        });

        btnVerTodo.setOnAction(e -> {
            if (avlActual == null) return;
            dibujarComprimido(avlActual);
        });

        btnInOrder.setOnAction(e -> {
            avlActual = obtenerAVL();
            if (avlActual == null) return;
            txtInOrder.clear();
            ListaEnlazada<Productos> lista = avlActual.inOrden();
            StringBuilder sb = new StringBuilder("InOrder (" + lista.size() + " productos): ");
            Nodo<Productos> n = lista.getHead();
            int i = 0;
            while (n != null) {
                sb.append(n.producto.getName());
                if (n.next != null) sb.append(" → ");
                n = n.next;
                i++;
                if (i % 20 == 0) sb.append("\n");
            }
            txtInOrder.setText(sb.toString());
        });

        this.getChildren().addAll(titulo, new Separator(), controles1, controles2, lblInfo, scroll, txtInOrder, panelExport);
    }

    private void dibujarNiveles(ArbolAVL<Productos> avl) {
        int alturaTotal = avl.getHeight();
        int nivelFin = Math.min(nivelActual + nivelesMostrar - 1, alturaTotal - 1);

        int nodosPorNivelFin = (int) Math.pow(2, nivelFin);
        double anchoNec = nodosPorNivelFin * (RADIO * 2 + 12);
        anchoNec = Math.min(anchoNec, MAX_CANVAS_W);

        double altoNec = (nivelFin - nivelActual + 1) * SEP_V + 80;

        canvas.setWidth(Math.max(1200, anchoNec));
        canvas.setHeight(Math.max(400, altoNec));

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Encabezado
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("Mostrando niveles " + nivelActual + " → " + nivelFin + " de " + (alturaTotal - 1) + " | Total nodos: " + avl.inOrden().size(), 10, 18);

        if (nivelActual > 0) {
            gc.setFill(Color.web("#2980b9"));
            gc.setFont(Font.font(11));
            gc.fillText("↑ Hay " + nivelActual + " nivel(es) arriba", 10, 35);
        }
        if (nivelFin < alturaTotal - 1) {
            gc.setFill(Color.web("#2980b9"));
            gc.setFont(Font.font(11));
            gc.fillText("↓ Hay " + (alturaTotal - 1 - nivelFin) + " nivel(es) abajo", 10, altoNec - 8);
        }

        lblNivel.setText("Nivel: " + nivelActual + " → " + nivelFin);


        dibujarRango(gc, avl.getRoot(), canvas.getWidth() / 2, 50, canvas.getWidth() / 4, 0, nivelActual, nivelFin, true);
    }

    private void dibujarRango(GraphicsContext gc, NodoAVL<Productos> nodo, double x, double y, double offset, int nivelNodo, int nivelMin, int nivelMax, boolean esRaiz) {
        if (nodo == null) return;


        if (nivelNodo > nivelMax) return;

        double hijoY = y + SEP_V;
        double offH = Math.max(offset / 2, 20);

        double yDibujado = nivelNodo >= nivelMin ? 50 + (nivelNodo - nivelMin) * SEP_V : -999; // no dibujar

        if (nivelNodo >= nivelMin && nivelNodo < nivelMax) {
            if (nodo.left != null) {
                double hx = x - offset;
                double hyD = 50 + (nivelNodo - nivelMin + 1) * SEP_V;
                gc.setStroke(Color.web("#bdc3c7"));
                gc.setLineWidth(1.2);
                gc.strokeLine(x, yDibujado, hx, hyD);
            }
            if (nodo.right != null) {
                double hx = x + offset;
                double hyD = 50 + (nivelNodo - nivelMin + 1) * SEP_V;
                gc.setStroke(Color.web("#bdc3c7"));
                gc.setLineWidth(1.2);
                gc.strokeLine(x, yDibujado, hx, hyD);
            }
        }

        if (nivelNodo >= nivelMin && nivelNodo <= nivelMax) {
            pintarNodo(gc, nodo, x, yDibujado, nivelNodo == 0, nivelNodo);
        }

        dibujarRango(gc, nodo.left, x - offset, hijoY, offH, nivelNodo + 1, nivelMin, nivelMax, false);
        dibujarRango(gc, nodo.right, x + offset, hijoY, offH, nivelNodo + 1, nivelMin, nivelMax, false);
    }


    private void dibujarComprimido(ArbolAVL<Productos> avl) {
        int altura = avl.getHeight();
        int nNodos = avl.inOrden().size();

        // Radio muy pequeño para que quepa
        int radio = Math.max(4, Math.min(14, 12000 / (int) Math.pow(2, altura)));
        int sepV = Math.max(25, radio * 3);

        double anchoNec = Math.pow(2, altura) * (radio * 2 + 4);
        anchoNec = Math.min(anchoNec, MAX_CANVAS_W);

        double altoNec = altura * sepV + 60;
        altoNec = Math.min(altoNec, MAX_CANVAS_H);

        canvas.setWidth(anchoNec);
        canvas.setHeight(altoNec);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, anchoNec, altoNec);
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, anchoNec, altoNec);

        gc.setFill(Color.web("#ecf0f1"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("Vista comprimida | " + nNodos + " nodos | Altura: " + altura + " | Radio=" + radio + "px", 8, 14);

        lblNivel.setText("Vista: todos los " + altura + " niveles");

        dibujarComprimidoNodo(gc, avl.getRoot(), anchoNec / 2, 30, anchoNec / 4, 0, altura, radio, sepV, true);
    }

    private void dibujarComprimidoNodo(GraphicsContext gc, NodoAVL<Productos> nodo, double x, double y, double offset, int nivel, int alturaTotal, int radio, int sepV, boolean esRaiz) {
        if (nodo == null) return;

        gc.setLineWidth(0.8);
        if (nodo.left != null) {
            double hx = x - offset;
            double hy = y + sepV;
            gc.setStroke(nivel >= alturaTotal - 2 ? Color.web("#27ae6044") : Color.web("#ffffff22"));
            gc.strokeLine(x, y, hx, hy);
            dibujarComprimidoNodo(gc, nodo.left, hx, hy, offset / 2, nivel + 1, alturaTotal, radio, sepV, false);
        }
        if (nodo.right != null) {
            double hx = x + offset;
            double hy = y + sepV;
            gc.setStroke(nivel >= alturaTotal - 2 ? Color.web("#27ae6044") : Color.web("#ffffff22"));
            gc.strokeLine(x, y, hx, hy);
            dibujarComprimidoNodo(gc, nodo.right, hx, hy, offset / 2, nivel + 1, alturaTotal, radio, sepV, false);
        }

        double t = (double) nivel / Math.max(alturaTotal - 1, 1);
        Color color = esRaiz ? Color.web("#e74c3c") : interpolarColor(Color.web("#3498db"), Color.web("#27ae60"), t);

        gc.setFill(color);
        gc.fillOval(x - radio, y - radio, radio * 2, radio * 2);

        if (radio >= 8) {
            String txt = nodo.producto != null ? nodo.producto.getName().substring(0, Math.min(3, nodo.producto.getName().length())) : "";
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(Math.max(6, radio * 0.5)));
            gc.fillText(txt, x - txt.length() * 1.8, y + 3);
        }
    }


    private void pintarNodo(GraphicsContext gc, NodoAVL<Productos> nodo, double x, double y, boolean esRaiz, int nivel) {
        boolean esHoja = (nodo.left == null && nodo.right == null);
        int fb = altN(nodo.left) - altN(nodo.right);

        Color color = esRaiz ? Color.web("#e74c3c") : esHoja ? Color.web("#27ae60") : Math.abs(fb) > 1 ? Color.web("#e67e22") : Color.web("#2980b9");

        // Sombra
        gc.setFill(Color.web("#00000020"));
        gc.fillOval(x - RADIO + 2, y - RADIO + 2, RADIO * 2, RADIO * 2);

        // Nodo
        gc.setFill(color);
        gc.fillOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.8);
        gc.strokeOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);

        // Nombre del producto
        String nom = nodo.producto != null ? nodo.producto.getName() : "?";
        String txt = nom.length() > 6 ? nom.substring(0, 5) + "." : nom;
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 9));
        gc.fillText(txt, x - txt.length() * 3.2, y + 3);

        // Altura del nodo
        gc.setFill(Color.web("#ecf0f1"));
        gc.setFont(Font.font(8));
        gc.fillText("h=" + nodo.altura, x - 8, y + RADIO + 11);

        // Factor de balance
        String fbStr = "FB:" + fb;
        Color fbColor = Math.abs(fb) <= 1 ? Color.web("#27ae60") : Color.web("#e74c3c");
        gc.setFill(fbColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        gc.fillText(fbStr, x - 10, y + RADIO + 21);

        gc.setFill(Color.web("#95a5a6"));
        gc.setFont(Font.font(7));
        gc.fillText("N" + nivel, x - RADIO - 2, y - RADIO + 7);
    }


    private int altN(NodoAVL<Productos> n) {
        return (n == null) ? 0 : n.altura;
    }

    private Color interpolarColor(Color a, Color b, double t) {
        return new Color(a.getRed() + (b.getRed() - a.getRed()) * t, a.getGreen() + (b.getGreen() - a.getGreen()) * t, a.getBlue() + (b.getBlue() - a.getBlue()) * t, 1.0);
    }

    private void actualizarInfo(ArbolAVL<Productos> avl) {
        int n = avl.inOrden().size();
        lblInfo.setText(String.format("Nodos: %d | Altura: %d | O(log n) ≈ %.1f | " + "Usa ⬆⬇ para navegar niveles", n, avl.getHeight(), Math.log(Math.max(n, 1)) / Math.log(2)));
    }

    private ArbolAVL<Productos> obtenerAVL() {
        String sel = cmbSucursal.getValue();
        if (sel == null) {
            ArbolAVL<Productos> avl = state.getAvlGlobal();
            if (avl.isEmpty()) {
                mostrarVacio("AVL global vacío");
                return null;
            }
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
        ListaEnlazada<Sucursal> lista = state.getCargaCSV().getListaSucursales();
        Nodo<Sucursal> n = lista.getHead();
        while (n != null) {
            cmbSucursal.getItems().add(n.producto.getIdSucursal() + " - " + n.producto.getNameSucursal());
            n = n.next;
        }
        if (sel != null && cmbSucursal.getItems().contains(sel)) cmbSucursal.setValue(sel);
    }

    private void mostrarVacio(String msg) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.GRAY);
        gc.setFont(Font.font(14));
        gc.fillText(msg, 400, 200);
    }

    PanelExportacion panelExport = new PanelExportacion("Árbol AVL", () -> {
        ArbolAVL<Productos> avl = obtenerAVL();
        if (avl == null) return "";
        String nom = cmbSucursal.getValue() != null ? "AVL_" + cmbSucursal.getValue().split(" - ")[0] : "AVL_Global";
        return ExportarEstructuras.avlToDot(avl, nom.replace(" ", "_"));
    }, (ruta, fmt) -> {
        ArbolAVL<Productos> avl = obtenerAVL();
        if (avl == null) return false;
        return ExportarEstructuras.exportarAVLImagen(avl, ruta, fmt);
    });
}