package ui.estructuras_view;

import clases.Productos;
import clases.Sucursal;
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

public class ListaEnlazadaView extends VBox {

    private AppState         state;
    private Canvas           canvas;
    private ComboBox<String> cmbSucursal;
    private Label            lblInfo;
    private ListaEnlazada<Productos> listaActual;

    private static final int NODO_W  = 150;
    private static final int NODO_H  = 50;
    private static final int GAP_X   = 36;
    private static final int GAP_Y   = 30;
    private static final int PAD_X   = 20;
    private static final int PAD_Y   = 50;
    private static final int POR_FILA = 6; // nodos por fila

    public ListaEnlazadaView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label(
                "Visualización — Lista Enlazada"
        );
        titulo.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold;"
        );

        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("Seleccionar sucursal");
        recargarSucursales();

        Button btnDibujar  = new Button("🔗 Dibujar Lista");
        Button btnRefrescar = new Button("🔄 Refrescar");

        btnDibujar.setStyle(
                "-fx-background-color: #2980b9;" +
                        "-fx-text-fill: white; -fx-padding: 6 14;"
        );
        btnRefrescar.setStyle(
                "-fx-background-color: #7f8c8d;" +
                        "-fx-text-fill: white; -fx-padding: 6 14;"
        );

        lblInfo = new Label(
                "Estructura secuencial | Búsqueda: O(n) | " +
                        "Inserción al final: O(n)"
        );
        lblInfo.setStyle(
                "-fx-text-fill: #7f8c8d; -fx-font-size: 11px;"
        );

        HBox controles = new HBox(10,
                new Label("Sucursal:"), cmbSucursal,
                btnDibujar, btnRefrescar
        );

        // Canvas con ScrollPane
        canvas = new Canvas(1200, 500);
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPrefHeight(520);
        scroll.setFitToWidth(false);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        // Tabla para mostrar datos estructurados
        TableView<Productos> tabla = crearTabla();
        tabla.setPrefHeight(220);

        PanelExportacion panelExport = new PanelExportacion(
                "Lista Enlazada",
                () -> {
                    if (listaActual == null) return "";
                    String sel = cmbSucursal.getValue();
                    String nom = sel != null
                            ? "Lista_S" + sel.split(" - ")[0]
                            : "Lista";
                    return ExportarEstructuras.listaToDot(
                            listaActual, nom
                    );
                },
                (ruta, fmt) -> {
                    if (listaActual == null) return false;
                    return ExportarEstructuras
                            .exportarListaImagen(
                                    listaActual, ruta, fmt
                            );
                }
        );

        // ── Acciones ──
        btnDibujar.setOnAction(e -> {
            String sel = cmbSucursal.getValue();
            if (sel == null) {
                mostrarVacio("Selecciona una sucursal");
                return;
            }
            int id = Integer.parseInt(
                    sel.split(" - ")[0].trim()
            );
            Sucursal s = state.getCargaCSV()
                    .buscarSucursal(id);
            if (s == null) return;

            listaActual = s.getLista();
            lblInfo.setText(String.format(
                    "Lista Enlazada — %d productos | " +
                            "O(n) búsqueda | O(1) inserción al inicio",
                    listaActual.size()
            ));
            dibujar(listaActual);
            cargarTabla(listaActual, tabla);
        });

        btnRefrescar.setOnAction(e -> {
            recargarSucursales();
        });

        this.getChildren().addAll(
                titulo, new Separator(),
                controles, lblInfo,
                scroll,
                new Label("Datos de la lista:"),
                tabla,
                panelExport
        );
    }

    // ─────────────────────────────────────────
    // DIBUJAR LA LISTA EN CANVAS
    // ─────────────────────────────────────────

    private void dibujar(ListaEnlazada<Productos> lista) {
        int total  = lista.size();
        int filas  = (int) Math.ceil(
                (double) total / POR_FILA
        );
        int cols   = Math.min(total, POR_FILA);

        double canvasW = cols * (NODO_W + GAP_X)
                + PAD_X*2;
        double canvasH = filas * (NODO_H + GAP_Y)
                + PAD_Y + 30;

        canvas.setWidth(Math.max(1200, canvasW));
        canvas.setHeight(Math.max(400, canvasH));

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(),
                canvas.getHeight());
        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, canvas.getWidth(),
                canvas.getHeight());

        // Título
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText(
                "Lista Enlazada — " + total + " nodos",
                PAD_X, 22
        );

        // Nodo HEAD
        gc.setFill(Color.web("#e74c3c"));
        gc.fillRoundRect(PAD_X, 30, 60, 20, 6, 6);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText("HEAD", PAD_X+12, 44);

        // Flecha HEAD → primer nodo
        if (total > 0) {
            gc.setStroke(Color.web("#e74c3c"));
            gc.setLineWidth(2);
            gc.strokeLine(PAD_X+60, 40, PAD_X+80, 40);
            gc.strokeLine(PAD_X+80, 40,
                    PAD_X+80, PAD_Y+NODO_H/2.0);
            gc.strokeLine(PAD_X+80, PAD_Y+NODO_H/2.0,
                    PAD_X, PAD_Y+NODO_H/2.0);
        }

        Nodo<Productos> actual = lista.getHead();
        int idx = 0;

        while (actual != null) {
            int fila = idx / POR_FILA;
            int col  = idx % POR_FILA;

            double x = PAD_X + col * (NODO_W + GAP_X);
            double y = PAD_Y + fila * (NODO_H + GAP_Y);

            // ── Dibujar el nodo ──
            dibujarNodo(gc, actual.producto, x, y, idx);

            // ── Flecha al siguiente ──
            if (actual.next != null) {
                int fila2 = (idx+1) / POR_FILA;
                int col2  = (idx+1) % POR_FILA;
                double x2 = PAD_X + col2*(NODO_W+GAP_X);
                double y2 = PAD_Y + fila2*(NODO_H+GAP_Y);

                gc.setStroke(Color.web("#e74c3c"));
                gc.setLineWidth(2);

                if (fila == fila2) {
                    // Misma fila — flecha horizontal
                    double ax = x + NODO_W;
                    double ay = y + NODO_H/2.0;
                    gc.strokeLine(ax, ay, x2, ay);
                    // Punta
                    dibujarFlecha(gc, x2, ay, true);
                } else {
                    // Cambio de fila — flecha en L
                    double ax1 = x+NODO_W,
                            ay1 = y+NODO_H/2.0;
                    double ax2 = x+NODO_W+GAP_X/2.0,
                            ay2 = y+NODO_H+GAP_Y/2.0;
                    double ax3 = PAD_X - GAP_X/2.0;
                    double ay4 = y2+NODO_H/2.0;

                    gc.strokeLine(ax1, ay1, ax2, ay1);
                    gc.strokeLine(ax2, ay1, ax2, ay2);
                    gc.strokeLine(ax2, ay2, ax3, ay2);
                    gc.strokeLine(ax3, ay2, ax3, ay4);
                    gc.strokeLine(ax3, ay4, x2, ay4);
                    dibujarFlecha(gc, x2, ay4, true);
                }
            } else {
                // Último nodo → NULL
                double ax = x + NODO_W + 5;
                double ay = y + NODO_H/2.0;

                gc.setFill(Color.web("#e74c3c"));
                gc.setFont(Font.font(
                        "Arial", FontWeight.BOLD, 11
                ));
                gc.fillText("→ NULL", ax, ay+4);
            }

            actual = actual.next;
            idx++;
        }
    }

    private void dibujarNodo(GraphicsContext gc,
                             Productos p,
                             double x, double y,
                             int idx) {
        // Fondo del nodo
        gc.setFill(Color.web("#2980b9"));
        gc.fillRoundRect(x, y, NODO_W, NODO_H, 8, 8);

        // Separador data|next
        gc.setStroke(Color.web("#1a6fa0"));
        gc.setLineWidth(1);
        gc.strokeLine(x+NODO_W-25, y,
                x+NODO_W-25, y+NODO_H);

        // Borde
        gc.setStroke(Color.web("#1a5276"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, NODO_W, NODO_H, 8, 8);

        // Número de índice (pequeño, arriba izquierda)
        gc.setFill(Color.web("#85c1e9"));
        gc.setFont(Font.font(8));
        gc.fillText("["+idx+"]", x+3, y+10);

        // Nombre del producto
        String nom = p != null ? p.getName() : "?";
        if (nom.length() > 13)
            nom = nom.substring(0,12)+".";
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(
                "Arial", FontWeight.BOLD, 10
        ));
        gc.fillText(nom, x+4, y+26);

        // Código de barras (más pequeño)
        String cod = p != null ? p.getBarCode() : "";
        if (cod.length() > 13)
            cod = cod.substring(0,12)+".";
        gc.setFill(Color.web("#d6eaf8"));
        gc.setFont(Font.font(8));
        gc.fillText(cod, x+4, y+40);

        // Cuadrito "next" (a la derecha)
        gc.setFill(Color.web("#1a6fa0"));
        gc.fillRoundRect(
                x+NODO_W-23, y+NODO_H/2.0-6,
                18, 12, 3, 3
        );
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(7));
        gc.fillText("→",
                x+NODO_W-19, y+NODO_H/2.0+4);
    }

    private void dibujarFlecha(GraphicsContext gc,
                               double x, double y,
                               boolean horizontal) {
        gc.setFill(Color.web("#e74c3c"));
        if (horizontal) {
            double[] px = {x, x-8, x-8};
            double[] py = {y, y-4, y+4};
            gc.fillPolygon(px, py, 3);
        } else {
            double[] px = {x-4, x+4, x};
            double[] py = {y-8, y-8, y};
            gc.fillPolygon(px, py, 3);
        }
    }

    // ─────────────────────────────────────────
    // TABLA DE PRODUCTOS
    // ─────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private TableView<Productos> crearTabla() {
        TableView<Productos> tabla = new TableView<>();
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        String[][] cols = {
                {"#",          "idx"},
                {"Nombre",     "name"},
                {"Código",     "barCode"},
                {"Categoría",  "category"},
                {"Caducidad",  "expiryDate"},
                {"Precio",     "price"},
                {"Stock",      "stock"}
        };

        // Columna índice
        TableColumn<Productos, String> colIdx =
                new TableColumn<>("#");
        colIdx.setPrefWidth(40);
        colIdx.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(
                                tabla.getItems().indexOf(d.getValue())
                        )
                )
        );
        tabla.getColumns().add(colIdx);

        // Columnas de datos
        String[][] dCols = {
                {"Nombre",     "name"},
                {"Código",     "barCode"},
                {"Categoría",  "category"},
                {"Caducidad",  "expiryDate"},
                {"Precio",     "price"},
                {"Stock",      "stock"},
                {"Marca",      "brand"}
        };

        for (String[] dc : dCols) {
            TableColumn<Productos, String> col =
                    new TableColumn<>(dc[0]);
            final String campo = dc[1];
            col.setCellValueFactory(d -> {
                Productos productos = d.getValue();
                String val = switch (campo) {
                    case "name"       -> productos.getName();
                    case "barCode"    -> productos.getBarCode();
                    case "category"   -> productos.getCategory();
                    case "expiryDate" -> productos.getExpiryDate();
                    case "price"      ->
                            String.format("Q%.2f",productos.getPrice());
                    case "stock"      ->
                            String.valueOf(productos.getStock());
                    case "brand"      -> productos.getBrand();
                    default           -> "";
                };
                return new javafx.beans.property
                        .SimpleStringProperty(val);
            });
            tabla.getColumns().add(col);
        }
        return tabla;
    }

    private void cargarTabla(ListaEnlazada<Productos> lista,
                             TableView<Productos> tabla) {
        tabla.getItems().clear();
        Nodo<Productos> n = lista.getHead();
        while (n != null) {
            tabla.getItems().add(n.producto);
            n = n.next;
        }
    }


    private void recargarSucursales() {
        String sel = cmbSucursal.getValue();
        cmbSucursal.getItems().clear();
        ListaEnlazada<Sucursal> lista =
                state.getCargaCSV().getListaSucursales();
        Nodo<Sucursal> n = lista.getHead();
        while (n != null) {
            cmbSucursal.getItems().add(
                    n.producto.getIdSucursal() +
                            " - " + n.producto.getNameSucursal()
            );
            n = n.next;
        }
        if (sel != null &&
                cmbSucursal.getItems().contains(sel))
            cmbSucursal.setValue(sel);
    }

    private void mostrarVacio(String msg) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(),
                canvas.getHeight());
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvas.getWidth(),
                canvas.getHeight());
        gc.setFill(Color.GRAY);
        gc.setFont(Font.font(14));
        gc.fillText(msg, 400, 200);
    }
}