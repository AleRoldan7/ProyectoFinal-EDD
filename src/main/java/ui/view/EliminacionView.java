package ui.view;

import clases.Productos;
import clases.Sucursal;
import estructuras.arbolB.ArbolB;
import estructuras.arbolB.NodoB;
import estructuras.arbolBPlus.ArbolBPlus;
import estructuras.arbolBPlus.NodoBPlus;
import estructuras.avl.ArbolAVL;
import estructuras.avl.NodoAVL;
import estructuras.lista.ListaEnlazada;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class EliminacionView extends VBox {

    private AppState appState;

    public EliminacionView(AppState appState) {

        this.appState = appState;
        this.setSpacing(0);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(tabEliminarProductoSucursal(), tabEliminarProductoGlobal(), tabEliminarSucursal(), tabRebalanceo());

        this.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
    }


    private Tab tabEliminarProductoSucursal() {
        Tab tab = new Tab("Eliminar en Sucursal");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Eliminar producto de una sucursal específica");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label desc = new Label("El producto se elimina de AVL, Hash, Árbol B, " + "Árbol B+ y Lista de esa sucursal. " + "Los árboles se rebalancean automáticamente.");
        desc.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        desc.setWrapText(true);

        ComboBox<String> cmbSuc = new ComboBox<>();
        cmbSuc.setPromptText("Selecciona sucursal");
        cmbSuc.setPrefWidth(280);
        recargarSucursales(cmbSuc);

        ComboBox<String> cmbProd = new ComboBox<>();
        cmbProd.setPromptText("Selecciona sucursal primero");
        cmbProd.setPrefWidth(350);

        cmbSuc.setOnAction(e -> {
            cargarProductos(cmbSuc, cmbProd);
        });

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("O escribe el código de barras");
        txtCodigo.setPrefWidth(280);

        Button btnBuscar = new Button("[Buscar]");
        Button btnEliminar = new Button("[Eliminar]");
        Button btnDeshacer = new Button("[Deshacer]");

        btnBuscar.setStyle(estilo("#2980b9"));
        btnEliminar.setStyle(estilo("#e74c3c"));
        btnDeshacer.setStyle(estilo("#e67e22"));

        btnBuscar.setText("Buscar");
        btnEliminar.setText("Eliminar");
        btnDeshacer.setText("Deshacer");

        TextArea txtPrevia = new TextArea();
        txtPrevia.setEditable(false);
        txtPrevia.setPrefHeight(80);
        txtPrevia.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        Label lblAVL = new Label("AVL de la sucursal (antes y después):");
        Canvas canvasAntes = new Canvas(450, 260);
        Canvas canvasDespues = new Canvas(450, 260);
        HBox canvases = new HBox(10, new VBox(5, new Label("Antes:"), canvasAntes), new VBox(5, new Label("Después:"), canvasDespues));

        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(130);
        log.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnBuscar.setOnAction(e -> {
            String cod = txtCodigo.getText().trim();
            String sel = cmbSuc.getValue();
            if (cod.isEmpty() || sel == null) {
                log.appendText("Ingresa código y sucursal\n");
                return;
            }
            int id = parsearId(sel);
            Sucursal s = appState.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            Productos p = s.buscarPorCodigo(cod);
            if (p != null) {
                txtPrevia.setText(formatearProducto(p));
                log.appendText("Encontrado: " + p.getName() + "\n");
                dibujarAVL(canvasAntes, s.getAvlNombre(), null, "Estado actual");
            } else {
                txtPrevia.setText("Producto no encontrado: " + cod);
                log.appendText("No encontrado: " + cod + "\n");
            }
        });

        btnEliminar.setOnAction(e -> {
            String cod = obtenerCodigo(cmbProd, txtCodigo);
            String sel = cmbSuc.getValue();
            if (cod == null || sel == null) {
                log.appendText("Selecciona sucursal y producto\n");
                return;
            }
            int id = parsearId(sel);
            Sucursal s = appState.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            dibujarAVL(canvasAntes, s.getAvlNombre(), null, "Antes");

            Productos p = s.buscarPorCodigo(cod);
            if (p == null) {
                log.appendText("Producto no existe: " + cod + "\n");
                return;
            }

            String nombre = p.getName();
            boolean ok = s.eliminarProducto(cod);

            if (ok) {
                dibujarAVL(canvasDespues, s.getAvlNombre(), null, "Después (rebalanceado)");
                log.appendText(String.format("'%s' eliminado de %s\n" + "   AVL rebalanceado | Hash actualizado\n" + "   Árbol B y B+ actualizados\n", nombre, s.getNameSucursal()));
                txtPrevia.clear();
                cargarProductos(cmbSuc, cmbProd);
            } else {
                log.appendText("Error al eliminar: " + cod + "\n");
            }
        });

        btnDeshacer.setOnAction(e -> {
            String sel = cmbSuc.getValue();
            if (sel == null) {
                log.appendText("Selecciona una sucursal\n");
                return;
            }
            int id = parsearId(sel);
            Sucursal s = appState.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            clases.OperacionProducto op = s.deshacerUltimaOperacion();
            if (op != null) {
                dibujarAVL(canvasDespues, s.getAvlNombre(), null, "Después del rollback");
                log.appendText("Deshecho: " + op + "\n");
                cargarProductos(cmbSuc, cmbProd);
            } else {
                log.appendText("Nada que deshacer\n");
            }
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.addRow(0, etiq("Sucursal:"), cmbSuc);
        form.addRow(1, etiq("Producto:"), cmbProd);
        form.addRow(2, etiq("Código:"), txtCodigo, btnBuscar);

        HBox botones = new HBox(10, btnEliminar, btnDeshacer);

        root.getChildren().addAll(titulo, desc, new Separator(), form, botones, new Label("Vista previa:"), txtPrevia, lblAVL, canvases, new Label("Log:"), log);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }


    private Tab tabEliminarProductoGlobal() {
        Tab tab = new Tab("Eliminar Global");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Eliminar producto de TODAS las sucursales");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label desc = new Label("Busca el código de barras en todas las sucursales " + "y lo elimina donde exista. " + "Cada árbol afectado se rebalancea.");
        desc.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        desc.setWrapText(true);

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Código de barras");
        txtCodigo.setPrefWidth(280);

        Button btnBuscarG = new Button("Buscar en todas");
        Button btnElimG = new Button("Eliminar de todas");

        btnBuscarG.setStyle(estilo("#2980b9"));
        btnElimG.setStyle(estilo("#e74c3c"));

        TextArea txtResultados = new TextArea();
        txtResultados.setEditable(false);
        txtResultados.setPrefHeight(150);
        txtResultados.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(200);
        log.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnBuscarG.setOnAction(e -> {
            String cod = txtCodigo.getText().trim();
            if (cod.isEmpty()) {
                log.appendText("Ingresa un código\n");
                return;
            }
            txtResultados.clear();
            txtResultados.appendText("Buscando '" + cod + "' en todas las sucursales:\n\n");

            ListaEnlazada<Sucursal> sucursales = appState.getCargaCSV().getListaSucursales();
            boolean encontrado = false;

            estructuras.nodo.Nodo<Sucursal> nodo = sucursales.getHead();
            while (nodo != null) {
                Sucursal s = nodo.producto;
                Productos p = s.buscarPorCodigo(cod);
                if (p != null) {
                    txtResultados.appendText(String.format("Encontrado en: %-20s → %s\n", s.getNameSucursal(), formatearProducto(p)));
                    encontrado = true;
                } else {
                    txtResultados.appendText(String.format("No existe en:  %s\n", s.getNameSucursal()));
                }
                nodo = nodo.next;
            }

            if (!encontrado) {
                txtResultados.appendText("\nCódigo no encontrado en ninguna sucursal\n");
            }
        });

        btnElimG.setOnAction(e -> {
            String cod = txtCodigo.getText().trim();
            if (cod.isEmpty()) {
                log.appendText("Ingresa un código\n");
                return;
            }

            log.appendText("=== Eliminación global: " + cod + " ===\n");

            ListaEnlazada<Sucursal> sucursales = appState.getCargaCSV().getListaSucursales();
            int eliminados = 0;

            estructuras.nodo.Nodo<Sucursal> nodo = sucursales.getHead();
            while (nodo != null) {
                Sucursal s = nodo.producto;
                Productos p = s.buscarPorCodigo(cod);

                if (p != null) {
                    String nombre = p.getName();
                    int altAntes = s.getAvlNombre().getHeight();

                    boolean ok = s.eliminarProducto(cod);

                    if (ok) {
                        int altDespues = s.getAvlNombre().getHeight();
                        eliminados++;
                        log.appendText(String.format("Eliminado de %-20s | " + "AVL: altura %d->%d (rebalanceado)\n", s.getNameSucursal(), altAntes, altDespues));
                    } else {
                        log.appendText(String.format("Error en %-20s\n", s.getNameSucursal()));
                    }
                }
                nodo = nodo.next;
            }

            Productos tempBuscar = buscarEnGlobal(cod);
            if (tempBuscar != null) {
                appState.getAvlGlobal().delete(tempBuscar);
                log.appendText("Eliminado del AVL global\n");
            }

            log.appendText(String.format("--- Total eliminados: %d sucursal(es) ---\n\n", eliminados));

            txtCodigo.clear();
            txtResultados.clear();
        });

        HBox botones = new HBox(10, btnBuscarG, btnElimG);

        root.getChildren().addAll(titulo, desc, new Separator(), new HBox(10, etiq("Código:"), txtCodigo), botones, new Label("Resultados de búsqueda:"), txtResultados, new Label("Log de eliminación:"), log);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }

    private Tab tabEliminarSucursal() {
        Tab tab = new Tab("Eliminar Sucursal");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Eliminar sucursal completa");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label desc = new Label("Elimina la sucursal y todos sus productos del sistema. " + "Los productos pueden transferirse antes de eliminar.");
        desc.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;" + "-fx-font-weight: bold;");
        desc.setWrapText(true);

        ComboBox<String> cmbSuc = new ComboBox<>();
        cmbSuc.setPromptText("Selecciona sucursal a eliminar");
        cmbSuc.setPrefWidth(300);
        recargarSucursales(cmbSuc);

        TextArea txtInfo = new TextArea();
        txtInfo.setEditable(false);
        txtInfo.setPrefHeight(120);
        txtInfo.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        Button btnVerInfo = new Button("Ver informacion");
        Button btnEliminar = new Button("Eliminar sucursal");
        Button btnRefrescar = new Button("Refrescar");

        btnVerInfo.setStyle(estilo("#2980b9"));
        btnEliminar.setStyle(estilo("#e74c3c"));
        btnRefrescar.setStyle(estilo("#7f8c8d"));

        CheckBox chkConfirmar = new CheckBox("Confirmo que quiero eliminar esta sucursal " + "y todos sus productos");
        chkConfirmar.setStyle("-fx-text-fill: #e74c3c;");

        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(200);
        log.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnRefrescar.setOnAction(e -> {
            recargarSucursales(cmbSuc);
            log.appendText("Lista actualizada\n");
        });

        btnVerInfo.setOnAction(e -> {
            String sel = cmbSuc.getValue();
            if (sel == null) {
                log.appendText("Selecciona una sucursal\n");
                return;
            }
            int id = parsearId(sel);
            Sucursal s = appState.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            txtInfo.setText(String.format("ID:          %d\n" + "Nombre:      %s\n" + "Ubicación:   %s\n" + "Productos:   %d\n" + "T.Ingreso:   %ds\n" + "T.Traspaso:  %ds\n" + "T.Despacho:  %ds\n" + "Cola ingreso: %d\n" + "Cola salida:  %d\n" + "AVL altura:  %d\n", s.getIdSucursal(), s.getNameSucursal(), s.getLocation(), s.getLista().size(), s.getEntryTime(), s.getTransferTime(), s.getDispatchInterval(), s.getColaIngreso().size(), s.getColaSalida().size(), s.getAvlNombre().getHeight()));
        });

        btnEliminar.setOnAction(e -> {
            if (!chkConfirmar.isSelected()) {
                log.appendText("Marca la casilla de confirmación\n");
                return;
            }
            String sel = cmbSuc.getValue();
            if (sel == null) {
                log.appendText("Selecciona una sucursal\n");
                return;
            }
            int id = parsearId(sel);
            Sucursal s = appState.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            int productos = s.getLista().size();
            String nombre = s.getNameSucursal();

            estructuras.nodo.Nodo<clases.Productos> nodo = s.getLista().getHead();
            while (nodo != null) {
                appState.getAvlGlobal().delete(nodo.producto);
                nodo = nodo.next;
            }

            log.appendText(String.format("=== Eliminando sucursal: %s ===\n", nombre));
            log.appendText(String.format("   Productos eliminados del AVL global: %d\n", productos));

            boolean eliminada = appState.getCargaCSV().eliminarSucursal(id);

            if (eliminada) {
                log.appendText(String.format("Sucursal '%s' eliminada correctamente\n", nombre));
                txtInfo.clear();
                chkConfirmar.setSelected(false);
                recargarSucursales(cmbSuc);
            } else {
                log.appendText("Error al eliminar sucursal\n");
            }
        });

        HBox botones = new HBox(10, btnVerInfo, btnRefrescar, btnEliminar);

        root.getChildren().addAll(titulo, desc, new Separator(), new HBox(10, etiq("Sucursal:"), cmbSuc), botones, new Label("Información:"), txtInfo, chkConfirmar, new Label("Log:"), log);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }


    private Tab tabRebalanceo() {
        Tab tab = new Tab("Rebalanceo");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Visualizar rebalanceo de estructuras");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> cmbSuc = new ComboBox<>();
        cmbSuc.setPromptText("Selecciona sucursal");
        cmbSuc.setPrefWidth(280);
        recargarSucursales(cmbSuc);

        ComboBox<String> cmbEstructura = new ComboBox<>();
        cmbEstructura.getItems().addAll("AVL (por nombre)", "Árbol B (por fecha)", "Árbol B+ (por categoría)");
        cmbEstructura.setValue("AVL (por nombre)");
        cmbEstructura.setPrefWidth(220);

        Button btnAnalizar = new Button("Analizar estructura");
        btnAnalizar.setStyle(estilo("#8e44ad"));

        Canvas canvasRebal = new Canvas(850, 320);
        canvasRebal.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");

        TextArea txtAnalisis = new TextArea();
        txtAnalisis.setEditable(false);
        txtAnalisis.setPrefHeight(200);
        txtAnalisis.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnAnalizar.setOnAction(e -> {
            String sel = cmbSuc.getValue();
            if (sel == null) {
                txtAnalisis.appendText("Selecciona una sucursal\n");
                return;
            }
            int id = parsearId(sel);
            Sucursal s = appState.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            String estructura = cmbEstructura.getValue();
            txtAnalisis.clear();

            if (estructura.startsWith("AVL")) {
                analizarAVL(s, canvasRebal, txtAnalisis);
            } else if (estructura.startsWith("Árbol B+")) {
                analizarBMas(s, canvasRebal, txtAnalisis);
            } else {
                analizarB(s, canvasRebal, txtAnalisis);
            }
        });

        HBox controles = new HBox(12, etiq("Sucursal:"), cmbSuc, etiq("Estructura:"), cmbEstructura, btnAnalizar);

        root.getChildren().addAll(titulo, new Separator(), controles, canvasRebal, new Label("Análisis de balance:"), txtAnalisis);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }


    private void dibujarAVL(Canvas canvas, ArbolAVL<Productos> avl, String resaltado, String titulo) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText(titulo != null ? titulo : "", 8, 16);

        if (avl == null || avl.isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.fillText("(vacío)", canvas.getWidth() / 2 - 20, canvas.getHeight() / 2);
            return;
        }
        dibujarNodoAVL(gc, avl.getRoot(), canvas.getWidth() / 2, 35, canvas.getWidth() / 4, resaltado);
    }

    private void dibujarNodoAVL(GraphicsContext gc, NodoAVL<Productos> nodo, double x, double y, double offset, String resaltado) {
        if (nodo == null) return;

        int altura = alturaAVL(nodo);
        int R = Math.max(18, 40 - altura * 2);
        String etiq = nodo.getProducto() != null ? (nodo.getProducto().getName().length() > 10 ? nodo.getProducto().getName().substring(0, 15) + "." : nodo.getProducto().getName()) : "?";

        boolean resalta = resaltado != null && nodo.getProducto() != null && nodo.getProducto().getName().equals(resaltado);

        if (nodo.left != null) {
            double cx = x - offset, cy = y + 60;
            gc.setStroke(Color.web("#bdc3c7"));
            gc.setLineWidth(1.5);
            gc.strokeLine(x, y + R, cx, cy - R);
            dibujarNodoAVL(gc, nodo.left, cx, cy, offset / 2, resaltado);
        }
        if (nodo.right != null) {
            double cx = x + offset, cy = y + 60;
            gc.setStroke(Color.web("#bdc3c7"));
            gc.setLineWidth(1.5);
            gc.strokeLine(x, y + R, cx, cy - R);
            dibujarNodoAVL(gc, nodo.right, cx, cy, offset / 2, resaltado);
        }

        gc.setFill(Color.web("#00000018"));
        gc.fillOval(x - R + 2, y - R + 2, R * 2, R * 2);

        Color fondo = resalta ? Color.web("#e74c3c") : Color.web("#2980b9");
        gc.setFill(fondo);
        gc.fillOval(x - R, y - R, R * 2, R * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(x - R, y - R, R * 2, R * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, R * 0.4));
        gc.fillText(etiq, x - etiq.length() * 2.8, y + 3);

        int fb = nodo.getAltura();
        String fbStr = "h:" + fb;
        gc.setFont(Font.font(7));
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillText(fbStr, x - 8, y + R + 10);
    }


    private void analizarAVL(Sucursal s, Canvas canvas, TextArea txt) {
        ArbolAVL<Productos> avl = s.getAvlNombre();

        if (avl.isEmpty()) {
            txt.appendText("AVL vacío en " + s.getNameSucursal() + "\n");
            return;
        }

        int[] stats = new int[4];
        stats[2] = 0;
        stats[3] = Integer.MAX_VALUE;
        analizarNodo(avl.getRoot(), stats, 0);

        double alturaIdeal = Math.log(stats[0] + 1) / Math.log(2);
        double eficiencia = alturaIdeal / avl.getHeight() * 100;

        txt.appendText(String.format("Análisis AVL %s \n\n", s.getNameSucursal()));
        txt.appendText(String.format("Nodos totales:    %d\n", stats[0]));
        txt.appendText(String.format("Hojas:            %d\n", stats[1]));
        txt.appendText(String.format("Altura actual:    %d\n", avl.getHeight()));
        txt.appendText(String.format("Altura ideal:     %.1f (log2 n)\n", alturaIdeal));
        txt.appendText(String.format("Eficiencia:       %.1f%%\n", eficiencia));
        txt.appendText(String.format("Nivel más hondo:  %d\n", stats[2]));
        txt.appendText(String.format("Nivel más bajo:   %d\n\n", stats[3]));

        boolean[] balanceOk = {true};
        verificarBalance(avl.getRoot(), balanceOk);
        txt.appendText(balanceOk[0] ? "Árbol correctamente balanceado \n" : "Nodo desbalanceado detectado\n");

        txt.appendText("\nOrden Big-O:\n" + "  Búsqueda: O(log n) = O(" + avl.getHeight() + ")\n" + "  Inserción: O(log n)\n" + "  Eliminación: O(log n)\n");

        dibujarAVL(canvas, avl, null, "AVL — " + s.getNameSucursal());
    }

    private void analizarNodo(NodoAVL<Productos> n, int[] stats, int nivel) {
        if (n == null) return;
        stats[0]++;
        if (n.left == null && n.right == null) {
            stats[1]++;
            stats[2] = Math.max(stats[2], nivel);
            stats[3] = Math.min(stats[3], nivel);
        }
        analizarNodo(n.left, stats, nivel + 1);
        analizarNodo(n.right, stats, nivel + 1);
    }

    private void verificarBalance(NodoAVL<Productos> n, boolean[] ok) {
        if (n == null) return;
        int izq = alturaAVL(n.left);
        int der = alturaAVL(n.right);
        if (Math.abs(izq - der) > 1) ok[0] = false;
        verificarBalance(n.left, ok);
        verificarBalance(n.right, ok);
    }

    private int alturaAVL(NodoAVL<Productos> n) {
        if (n == null) return -1;
        return 1 + Math.max(alturaAVL(n.left), alturaAVL(n.right));
    }

    private void analizarB(Sucursal s, Canvas canvas, TextArea txt) {
        txt.appendText(String.format("Análisis Árbol B %s \n\n", s.getNameSucursal()));
        txt.appendText("Estructura B con claves ordenadas por fecha de vencimiento.\n");
        txt.appendText("Orden Big-O:\n");
        txt.appendText("  Búsqueda: O(log_t n)\n");
        txt.appendText("  Inserción: O(log_t n)\n");
        txt.appendText("  Eliminación: O(log_t n)\n");
        dibujarArbolBCanvas(canvas, s.getArbolBFechas());
    }

    private void analizarBMas(Sucursal s, Canvas canvas, TextArea txt) {

        txt.appendText("Árbol B+ - índices por categoría\n\n");

        if (s.getArbolBPlusCategoria() == null) {
            txt.appendText("Árbol vacío\n");
            return;
        }

        dibujarArbolBMasCanvas(canvas, s.getArbolBPlusCategoria());
    }
    private void dibujarArbolBCanvas(Canvas canvas, ArbolB<String> arbol) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (arbol == null || arbol.getRaiz() == null) {
            gc.setFill(Color.GRAY);
            gc.fillText("(vacío)", canvas.getWidth() / 2 - 20, canvas.getHeight() / 2);
            return;
        }
        dibujarNodoBCanvas(gc, arbol.getRaiz(), canvas.getWidth() / 2, 20, canvas.getWidth() / 4);
    }

    private void dibujarArbolBMasCanvas(Canvas canvas, ArbolBPlus<String> arbol) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (arbol == null || arbol.getRaiz() == null) {
            gc.setFill(Color.GRAY);
            gc.fillText("(vacío)", canvas.getWidth() / 2 - 20, canvas.getHeight() / 2);
            return;
        }
        dibujarNodoBMasCanvas(gc, arbol.getRaiz(), canvas.getWidth() / 2, 20, canvas.getWidth() / 4);
    }

    private void dibujarNodoBCanvas(GraphicsContext gc, NodoB<String> nodo, double cx, double cy, double off) {
        if (nodo == null || nodo.getNumClaves() == 0) return;

        int num = nodo.getNumClaves();
        int nW = 80;
        int nH = 24;
        double nodoW = num * nW;
        double nodoX = cx - nodoW / 2.0;

        gc.setFill(Color.web("#2c3e50"));
        gc.fillRoundRect(nodoX, cy, nodoW, nH, 6, 6);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(nodoX, cy, nodoW, nH, 6, 6);

        for (int i = 0; i < num; i++) {
            if (i > 0) {
                gc.setStroke(Color.web("#ffffff88"));
                gc.strokeLine(nodoX + i * nW, cy, nodoX + i * nW, cy + nH);
            }
            String clave = nodo.getClaves()[i] != null ? nodo.getClaves()[i].toString() : "";
            if (clave.length() > 10) clave = clave.substring(0, 9) + ".";
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(9));
            gc.fillText(clave, nodoX + i * nW + 4, cy + nH / 2.0 + 4);
        }

        if (!nodo.isEsHoja()) {
            int numH = num + 1;
            double paso = (off * 2) / Math.max(numH - 1, 1);
            double iniX = cx - off;
            for (int i = 0; i <= num; i++) {
                if (nodo.getHijos()[i] == null) continue;
                double hx = numH == 1 ? cx : iniX + i * paso;
                double hy = cy + 70;
                gc.setStroke(Color.web("#bdc3c7"));
                gc.setLineWidth(1);
                gc.strokeLine(cx, cy + nH, hx, hy);
                dibujarNodoBCanvas(gc, nodo.getHijos()[i], hx, hy, off / 2);
            }
        }
    }

    private void dibujarNodoBMasCanvas(GraphicsContext gc, NodoBPlus<String> nodo,
                                       double cx, double cy, double off) {

        if (nodo == null || nodo.getNumClaves() == 0) return;

        int num = nodo.getNumClaves();
        int nH = 28;

        int nW = 0;
        Comparable[] clavesArr = nodo.getClaves();

        for (int i = 0; i < num; i++) {
            String clave = clavesArr[i] != null ? clavesArr[i].toString() : "";
            nW = Math.max(nW, clave.length() * 7);
        }
        nW += 20;

        double nodoW = num * nW;
        double nodoX = cx - nodoW / 2.0;

        gc.setFill(nodo.isEsHoja() ? Color.web("#16a085") : Color.web("#2c3e50"));
        gc.fillRoundRect(nodoX, cy, nodoW, nH, 6, 6);

        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(nodoX, cy, nodoW, nH, 6, 6);

        for (int i = 0; i < num; i++) {

            if (i > 0) {
                gc.setStroke(Color.web("#ffffff88"));
                gc.strokeLine(nodoX + i * nW, cy, nodoX + i * nW, cy + nH);
            }

            String clave = clavesArr[i] != null ? clavesArr[i].toString() : "";

            if (clave.length() > 12)
                clave = clave.substring(0, 11) + "...";

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(10));

            double textX = nodoX + i * nW + (nW / 2.0) - (clave.length() * 3);
            gc.fillText(clave, textX, cy + nH / 2.0 + 4);
        }

        // hijos
        if (!nodo.isEsHoja()) {
            int numH = num + 1;
            double paso = (off * 2) / Math.max(numH - 1, 1);
            double iniX = cx - off;

            for (int i = 0; i <= num; i++) {
                if (nodo.getHijos()[i] == null) continue;

                double hx = numH == 1 ? cx : iniX + i * paso;
                double hy = cy + 90;

                gc.setStroke(Color.web("#bdc3c7"));
                gc.strokeLine(cx, cy + nH, hx, hy);

                dibujarNodoBMasCanvas(gc, nodo.getHijos()[i], hx, hy, off / 2);
            }
        }
    }
    private Productos buscarEnGlobal(String codigo) {
        ListaEnlazada<Sucursal> suc = appState.getCargaCSV().getListaSucursales();
        estructuras.nodo.Nodo<Sucursal> n = suc.getHead();
        while (n != null) {
            Productos p = n.producto.buscarPorCodigo(codigo);
            if (p != null) return p;
            n = n.next;
        }
        return null;
    }

    private void cargarProductos(ComboBox<String> cmbSuc, ComboBox<String> cmbProd) {
        cmbProd.getItems().clear();
        String sel = cmbSuc.getValue();
        if (sel == null) return;
        int id = parsearId(sel);
        Sucursal s = appState.getCargaCSV().buscarSucursal(id);
        if (s == null) return;
        estructuras.nodo.Nodo<clases.Productos> n = s.getLista().getHead();
        while (n != null) {
            cmbProd.getItems().add(n.producto.getBarCode() + " | " + n.producto.getName());
            n = n.next;
        }
    }

    private String obtenerCodigo(ComboBox<String> cmbProd, TextField txtCodigo) {
        String txt = txtCodigo.getText().trim();
        if (!txt.isEmpty()) return txt;
        String sel = cmbProd.getValue();
        if (sel == null) return null;
        return sel.split("\\|")[0].trim();
    }

    private void recargarSucursales(ComboBox<String> cmb) {
        String actual = cmb.getValue();
        cmb.getItems().clear();
        ListaEnlazada<Sucursal> lista = appState.getCargaCSV().getListaSucursales();
        estructuras.nodo.Nodo<Sucursal> n = lista.getHead();
        while (n != null) {
            cmb.getItems().add(n.producto.getIdSucursal() + " - " + n.producto.getNameSucursal());
            n = n.next;
        }
        if (actual != null && cmb.getItems().contains(actual)) cmb.setValue(actual);
    }

    private int parsearId(String s) {
        return Integer.parseInt(s.split(" - ")[0].trim());
    }

    private Label etiq(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight: bold;");
        l.setPrefWidth(85);
        return l;
    }

    private String estilo(String c) {
        return "-fx-background-color:" + c + "; -fx-text-fill: white; -fx-padding: 6 14;";
    }

    private String formatearProducto(Productos p) {
        return String.format("Nombre:   %s\nCódigo:   %s\n" + "Cat:      %s\nPrecio:   Q%.2f\n" + "Stock:    %d\nFecha:    %s", p.getName(), p.getBarCode(), p.getCategory(), p.getPrice(), p.getStock(), p.getExpiryDate());
    }
}