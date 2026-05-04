package ui.view;

import clases.Productos;
import clases.Sucursal;
import estructuras.lista.ListaEnlazada;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import ui.view.AppState;

import java.util.List;

public class ProductosView extends VBox {

    private AppState state;

    private ComboBox<String> cmbSucursal;
    private TextField txtNombre, txtCodigo, txtCategoria, txtFechaDesde, txtFechaHasta, txtMarca, txtPrecio, txtStock, txtFecha;
    private TableView<Productos> tablaResultados;
    private TableView<Productos> tablaInOrder;
    private TextArea logOperaciones;

    public ProductosView(AppState state) {
        this.state = state;
        this.setSpacing(0);
        this.setPadding(new Insets(0));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(crearTabAgregar(), crearTabListar());

        this.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }


    private Tab crearTabAgregar() {
        Tab tab = new Tab("Agregar Producto");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Registrar nuevo producto");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Selector de sucursal
        Label lblSuc = new Label("Sucursal destino:");
        lblSuc.setStyle("-fx-font-weight: bold;");
        cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("Selecciona una sucursal");
        cmbSucursal.setPrefWidth(280);
        recargarSucursales(cmbSucursal);

        // Formulario en grid
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 10, 0));

        txtNombre = campo("Nombre del producto");
        txtCodigo = campo("Código de barras");
        txtCategoria = campo("Categoría (ej: Lacteos)");
        txtFecha = campo("YYYY-MM-DD");
        txtMarca = campo("Marca");
        txtPrecio = campo("0.00");
        txtStock = campo("0");

        form.addRow(0, etiqueta("Nombre:"), txtNombre, etiqueta("Código:"), txtCodigo);
        form.addRow(1, etiqueta("Categoría:"), txtCategoria, etiqueta("Caducidad:"), txtFecha);
        form.addRow(2, etiqueta("Marca:"), txtMarca, etiqueta("Precio Q:"), txtPrecio);
        form.addRow(3, etiqueta("Stock:"), txtStock);

        // Columna 2 más ancha
        ColumnConstraints col1 = new ColumnConstraints(100);
        ColumnConstraints col2 = new ColumnConstraints(200);
        ColumnConstraints col3 = new ColumnConstraints(100);
        ColumnConstraints col4 = new ColumnConstraints(200);
        form.getColumnConstraints().addAll(col1, col2, col3, col4);

        Button btnAgregar = new Button("Agregar producto");
        Button btnLimpiar = new Button("Limpiar");
        btnAgregar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;" + "-fx-font-size: 13px; -fx-padding: 8 20;");
        btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;" + "-fx-padding: 8 16;");

        logOperaciones = new TextArea();
        logOperaciones.setEditable(false);
        logOperaciones.setPrefHeight(150);
        logOperaciones.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnAgregar.setOnAction(e -> agregarProducto());
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        HBox botones = new HBox(10, btnAgregar, btnLimpiar);

        root.getChildren().addAll(titulo, new Separator(), lblSuc, cmbSucursal, form, botones, new Label("Log de operaciones:"), logOperaciones);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }

    private Tab crearTabBuscar() {
        Tab tab = new Tab("Buscar");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Búsqueda avanzada de productos");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Selector de sucursal para búsqueda
        ComboBox<String> cmbSucBuscar = new ComboBox<>();
        cmbSucBuscar.setPromptText("Buscar en todas las sucursales");
        cmbSucBuscar.setPrefWidth(280);
        recargarSucursales(cmbSucBuscar);
        // Opción "todas"
        cmbSucBuscar.getItems().add(0, "-- Todas las sucursales --");

        // Campos de búsqueda
        TextField busNombre = campo("Nombre del producto");
        TextField busCodigo = campo("Código de barras exacto");
        TextField busCategoria = campo("Categoría");
        TextField busFechaD = campo("Desde YYYY-MM-DD");
        TextField busFechaH = campo("Hasta YYYY-MM-DD");

        GridPane formBus = new GridPane();
        formBus.setHgap(15);
        formBus.setVgap(10);
        formBus.addRow(0, etiqueta("Por nombre:"), busNombre);
        formBus.addRow(1, etiqueta("Por código:"), busCodigo);
        formBus.addRow(2, etiqueta("Por categoría:"), busCategoria);
        formBus.addRow(3, etiqueta("Rango fechas:"), new HBox(8, busFechaD, new Label("→"), busFechaH));

        Button btnBusNom = btnBusqueda("Buscar nombre (AVL) O(log n)", "#2980b9");
        Button btnBusCod = btnBusqueda("Buscar código (Hash) O(1)", "#8e44ad");
        Button btnBusCat = btnBusqueda("Buscar categoría (B+) O(log n)", "#d35400");
        Button btnBusFec = btnBusqueda("Buscar rango fecha (B) O(log n)", "#27ae60");
        Button btnBusSeq = btnBusqueda("Búsqueda secuencial (Lista) O(n)", "#e74c3c");

        // Tabla de resultados
        tablaResultados = crearTabla();
        Label lblTiempo = new Label("Tiempo de búsqueda: —");
        lblTiempo.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        btnBusNom.setOnAction(e -> {
            String txt = busNombre.getText().trim();
            if (txt.isEmpty()) return;
            long t0 = System.nanoTime();
            ListaEnlazada<Productos> res = buscarPorNombreAVL(txt, getSucursalSeleccionada(cmbSucBuscar));
            long t = System.nanoTime() - t0;
            mostrarEnTabla(res, tablaResultados);
            lblTiempo.setText(String.format("[AVL] %d resultado(s) en %,d ns", res.size(), t));
        });

        btnBusCod.setOnAction(e -> {
            String txt = busCodigo.getText().trim();
            if (txt.isEmpty()) return;
            long t0 = System.nanoTime();
            ListaEnlazada<Productos> res = buscarPorCodigoHash(txt, getSucursalSeleccionada(cmbSucBuscar));
            long t = System.nanoTime() - t0;
            mostrarEnTabla(res, tablaResultados);
            lblTiempo.setText(String.format("[Hash] %d resultado(s) en %,d ns", res.size(), t));
        });

        btnBusCat.setOnAction(e -> {
            String txt = busCategoria.getText().trim();
            if (txt.isEmpty()) return;
            long t0 = System.nanoTime();
            ListaEnlazada<Productos> res = buscarPorCategoriaBPlus(txt, getSucursalSeleccionada(cmbSucBuscar));
            long t = System.nanoTime() - t0;
            mostrarEnTabla(res, tablaResultados);
            lblTiempo.setText(String.format("[B+] %d resultado(s) en %,d ns", res.size(), t));
        });

        btnBusFec.setOnAction(e -> {
            String desde = busFechaD.getText().trim();
            String hasta = busFechaH.getText().trim();
            if (desde.isEmpty() || hasta.isEmpty()) return;
            long t0 = System.nanoTime();
            ListaEnlazada<Productos> res = buscarPorRangoFechaB(desde, hasta, getSucursalSeleccionada(cmbSucBuscar));
            long t = System.nanoTime() - t0;
            mostrarEnTabla(res, tablaResultados);
            lblTiempo.setText(String.format("[B] %d resultado(s) en %,d ns", res.size(), t));
        });

        btnBusSeq.setOnAction(e -> {
            String txt = busNombre.getText().trim();
            if (txt.isEmpty()) return;
            long t0 = System.nanoTime();
            ListaEnlazada<Productos> res = buscarSecuencial(txt, getSucursalSeleccionada(cmbSucBuscar));
            long t = System.nanoTime() - t0;
            mostrarEnTabla(res, tablaResultados);
            lblTiempo.setText(String.format("[Lista] %d resultado(s) en %,d ns", res.size(), t));
        });

        FlowPane botonesB = new FlowPane(8, 8, btnBusNom, btnBusCod, btnBusCat, btnBusFec, btnBusSeq);
        botonesB.setPrefWrapLength(600);

        root.getChildren().addAll(titulo, new Separator(), new HBox(10, etiqueta("Sucursal:"), cmbSucBuscar), formBus, botonesB, lblTiempo, new Label("Resultados:"), tablaResultados);

        VBox.setVgrow(tablaResultados, Priority.ALWAYS);
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }

    private Tab crearTabListar() {
        Tab tab = new Tab("Listar / InOrder AVL");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Listado completo de productos");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> cmbSucLista = new ComboBox<>();
        cmbSucLista.setPromptText("Selecciona sucursal");
        cmbSucLista.setPrefWidth(280);
        recargarSucursales(cmbSucLista);
        cmbSucLista.getItems().add(0, "-- Todas (AVL global) --");

        Button btnInOrder = new Button("InOrder AVL (A→Z)");
        Button btnPorSuc = new Button("Ver por sucursal");
        Button btnRecargar = new Button("Recargar");

        btnInOrder.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;" + "-fx-padding: 6 14;");
        btnPorSuc.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;" + "-fx-padding: 6 14;");
        btnRecargar.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;" + "-fx-padding: 6 14;");

        Label lblConteo = new Label("Total: 0 productos");
        lblConteo.setStyle("-fx-font-weight: bold;");

        tablaInOrder = crearTabla();
        VBox.setVgrow(tablaInOrder, Priority.ALWAYS);

        btnRecargar.setOnAction(e -> {
            recargarSucursales(cmbSucLista);
        });

        btnInOrder.setOnAction(e -> {
            ListaEnlazada<Productos> todos;
            String sel = cmbSucLista.getValue();

            if (sel == null || sel.startsWith("--")) {
                todos = state.getAvlGlobal().inOrden();
            } else {
                int id = Integer.parseInt(sel.split(" - ")[0].trim());
                Sucursal s = state.getCargaCSV().buscarSucursal(id);
                if (s != null) {
                    todos = s.getAvlNombre().inOrden();
                } else {
                    todos = new ListaEnlazada<>();
                }
            }

            mostrarEnTabla(todos, tablaInOrder);
            lblConteo.setText("Total: " + todos.size() + " productos (orden alfabético A→Z)");
        });

        btnPorSuc.setOnAction(e -> {
            String sel = cmbSucLista.getValue();
            if (sel == null || sel.startsWith("--")) {
                ListaEnlazada<Productos> todos = new ListaEnlazada<>();
                for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
                    todos.agregarTodos(s.getLista());
                }
                mostrarEnTabla(todos, tablaInOrder);
                lblConteo.setText("Total: " + todos.size() + " productos");
            } else {
                int id = Integer.parseInt(sel.split(" - ")[0].trim());
                Sucursal s = state.getCargaCSV().buscarSucursal(id);
                if (s != null) {
                    ListaEnlazada<Productos> lista = s.getLista();
                    mostrarEnTabla(lista, tablaInOrder);
                    lblConteo.setText(s.getNameSucursal() + ": " + lista.size() + " productos");
                }
            }
        });

        HBox botones = new HBox(10, btnInOrder, btnPorSuc, btnRecargar);

        root.getChildren().addAll(titulo, new Separator(), new HBox(10, etiqueta("Sucursal:"), cmbSucLista), botones, lblConteo, tablaInOrder);

        tab.setContent(root);
        return tab;
    }

    private Tab crearTabEliminar() {
        Tab tab = new Tab("Eliminar / Rollback");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Eliminar productos y deshacer operaciones");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> cmbSucEl = new ComboBox<>();
        cmbSucEl.setPromptText("Selecciona la sucursal");
        cmbSucEl.setPrefWidth(280);
        recargarSucursales(cmbSucEl);

        TextField txtCodigoEl = campo("Código de barras a eliminar");
        txtCodigoEl.setPrefWidth(280);

        Button btnBuscarEl = new Button("Buscar producto");
        Button btnEliminar = new Button("Eliminar");
        Button btnDeshacer = new Button("Deshacer última");

        btnBuscarEl.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;" + "-fx-padding: 6 14;");
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" + "-fx-padding: 8 20; -fx-font-size: 13px;");
        btnDeshacer.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;" + "-fx-padding: 8 16;");

        TableView<Productos> tablaPrevia = crearTabla();
        tablaPrevia.setPrefHeight(120);

        TextArea logEl = new TextArea();
        logEl.setEditable(false);
        logEl.setPrefHeight(180);
        logEl.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        btnBuscarEl.setOnAction(e -> {
            String cod = txtCodigoEl.getText().trim();
            String sel = cmbSucEl.getValue();
            if (cod.isEmpty() || sel == null) return;

            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            Productos p = s.buscarPorCodigo(cod);

            if (p != null) {
                ListaEnlazada<Productos> temp = new ListaEnlazada<>();
                temp.agregar(p);

                mostrarEnTabla(temp, tablaPrevia);

                logEl.appendText("Producto encontrado: " + p.getName() + "\n");
            } else {
                tablaPrevia.getItems().clear();
                logEl.appendText("Código no encontrado: " + cod + "\n");
            }
        });

        btnEliminar.setOnAction(e -> {
            String cod = txtCodigoEl.getText().trim();
            String sel = cmbSucEl.getValue();
            if (cod.isEmpty() || sel == null) {
                logEl.appendText("Selecciona sucursal e ingresa código\n");
                return;
            }
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            boolean ok = s.eliminarProducto(cod);
            if (ok) {
                tablaPrevia.getItems().clear();
                txtCodigoEl.clear();
                logEl.appendText("Producto eliminado: " + cod + "\n");
            } else {
                logEl.appendText("No se pudo eliminar: " + cod + "\n");
            }
        });

        btnDeshacer.setOnAction(e -> {
            String sel = cmbSucEl.getValue();
            if (sel == null) {
                logEl.appendText("Selecciona una sucursal\n");
                return;
            }
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s == null) return;

            clases.OperacionProducto op = s.deshacerUltimaOperacion();
            if (op != null) {
                logEl.appendText("↩ Deshecho: " + op + "\n");
            } else {
                logEl.appendText("No hay operaciones para deshacer\n");
            }
        });

        HBox botones = new HBox(10, btnBuscarEl, btnEliminar, btnDeshacer);

        root.getChildren().addAll(titulo, new Separator(), new HBox(10, etiqueta("Sucursal:"), cmbSucEl), new HBox(10, etiqueta("Código:"), txtCodigoEl), botones, new Label("Vista previa:"), tablaPrevia, new Label("Log:"), logEl);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }

    @SuppressWarnings("unchecked")
    private TableView<Productos> crearTabla() {
        TableView<Productos> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Productos, String> colNom = new TableColumn<>("Nombre");
        colNom.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<Productos, String> colCod = new TableColumn<>("Código");
        colCod.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBarCode()));
        colCod.setPrefWidth(130);

        TableColumn<Productos, String> colCat = new TableColumn<>("Categoría");
        colCat.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCategory()));

        TableColumn<Productos, String> colFec = new TableColumn<>("Caducidad");
        colFec.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getExpiryDate()));

        TableColumn<Productos, String> colPre = new TableColumn<>("Precio");
        colPre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("Q%.2f", d.getValue().getPrice())));
        colPre.setPrefWidth(80);

        TableColumn<Productos, String> colSto = new TableColumn<>("Stock");
        colSto.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getStock())));
        colSto.setPrefWidth(60);

        TableColumn<Productos, String> colMar = new TableColumn<>("Marca");
        colMar.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBrand()));

        TableColumn<Productos, String> colSuc = new TableColumn<>("Sucursal");
        colSuc.setCellValueFactory(d -> {
            Sucursal s = state.getCargaCSV().buscarSucursal(d.getValue().getBranchId());
            String nom = (s != null) ? s.getNameSucursal() : "ID:" + d.getValue().getBranchId();
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        tabla.getColumns().addAll(colNom, colCod, colCat, colFec, colPre, colSto, colMar, colSuc);
        return tabla;
    }

    private ListaEnlazada<Productos> buscarPorNombreAVL(String nombre, Sucursal filtro) {
        ListaEnlazada<Productos> res = new ListaEnlazada<>();
        ListaEnlazada<Sucursal> lista;

        if (filtro != null) {
            lista = new ListaEnlazada<>();
            lista.agregar(filtro);
        } else {
            lista = state.getCargaCSV().getListaSucursales();
        }

        for (Sucursal s : lista) {
            Productos p = s.buscarPorNombre(nombre);
            if (p != null) res.agregar(p);
        }
        return res;
    }

    private ListaEnlazada<Productos> buscarPorCodigoHash(String codigo, Sucursal filtro) {
        ListaEnlazada<Productos> res = new ListaEnlazada<>();
        ListaEnlazada<Sucursal> lista;

        if (filtro != null) {
            lista = new ListaEnlazada<>();
            lista.agregar(filtro);
        } else {
            lista = state.getCargaCSV().getListaSucursales();
        }

        for (Sucursal s : lista) {
            Productos p = s.buscarPorCodigo(codigo);
            if (p != null) res.agregar(p);
        }
        return res;
    }

    private ListaEnlazada<Productos> buscarPorCategoriaBPlus(String cat, Sucursal filtro) {
        ListaEnlazada<Productos> res = new ListaEnlazada<>();
        ListaEnlazada<Sucursal> lista;

        if (filtro != null) {
            lista = new ListaEnlazada<>();
            lista.agregar(filtro);
        } else {
            lista = state.getCargaCSV().getListaSucursales();
        }

        for (Sucursal s : lista) {
            ListaEnlazada<String> claves = s.buscarPorCategoria(cat);
            if (!claves.isEmpty()) {
                for (Productos p : s.getLista().toList()) {
                    if (p.getCategory().equalsIgnoreCase(cat)) res.agregar(p);
                }
            }
        }
        return res;
    }

    private ListaEnlazada<Productos> buscarPorRangoFechaB(String desde, String hasta, Sucursal filtro) {
        ListaEnlazada<Productos> res = new ListaEnlazada<>();
        ListaEnlazada<Sucursal> lista;

        if (filtro != null) {
            lista = new ListaEnlazada<>();
            lista.agregar(filtro);
        } else {
            lista = state.getCargaCSV().getListaSucursales();
        }

        for (Sucursal s : lista) {
            ListaEnlazada<String> fechas = s.buscarPorRangoFecha(desde, hasta);
            for (String fecha : fechas) {
                for (Productos p : s.getLista().toList()) {
                    if (p.getExpiryDate().equals(fecha)) res.agregar(p);
                }
            }
        }
        return res;
    }

    private ListaEnlazada<Productos> buscarSecuencial(String nombre, Sucursal filtro) {
        ListaEnlazada<Productos> res = new ListaEnlazada<>();
        ListaEnlazada<Sucursal> lista;

        if (filtro != null) {
            lista = new ListaEnlazada<>();
            lista.agregar(filtro);
        } else {
            lista = state.getCargaCSV().getListaSucursales();
        }

        for (Sucursal s : lista) {
            Productos p = s.getLista().buscar(prod -> prod.getName().equalsIgnoreCase(nombre));
            if (p != null) res.agregar(p);
        }
        return res;
    }

    private void agregarProducto() {
        try {
            String sel = cmbSucursal.getValue();
            if (sel == null) {
                log("Selecciona una sucursal");
                return;
            }
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s == null) {
                log("Sucursal no encontrada");
                return;
            }

            String nom = txtNombre.getText().trim();
            String cod = txtCodigo.getText().trim();
            String cat = txtCategoria.getText().trim();
            String fec = txtFecha.getText().trim();
            String mar = txtMarca.getText().trim();

            if (nom.isEmpty() || cod.isEmpty()) {
                log("Nombre y código son obligatorios");
                return;
            }

            if (cod.length() != 10) {
                log("EL codigo tiene que tener solo 10 caracteres");
                return;
            }

            double pre = Double.parseDouble(txtPrecio.getText().trim());
            int sto = Integer.parseInt(txtStock.getText().trim());

            Productos p = new Productos(id, nom, cod, cat, fec, mar, pre, sto);
            boolean ok = s.agregarProducto(p);

            if (ok) {
                state.getAvlGlobal().insert(p);
                log("Producto agregado: " + nom + " -> " + s.getNameSucursal());
                limpiarFormulario();
            } else {
                log("Error: ¿código duplicado? " + cod);
            }
        } catch (NumberFormatException ex) {
            log("Precio y Stock deben ser números");
        }
    }

    private void mostrarEnTabla(ListaEnlazada<Productos> lista, TableView<Productos> tabla) {
        tabla.getItems().clear();

        for (Productos productos : lista) {
            tabla.getItems().addAll(productos);
        }
    }

    private Sucursal getSucursalSeleccionada(ComboBox<String> cmb) {
        String sel = cmb.getValue();
        if (sel == null || sel.startsWith("--")) return null;
        int id = Integer.parseInt(sel.split(" - ")[0].trim());
        return state.getCargaCSV().buscarSucursal(id);
    }

    private void recargarSucursales(ComboBox<String> cmb) {
        String actual = cmb.getValue();
        cmb.getItems().clear();
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmb.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }
        if (actual != null && cmb.getItems().contains(actual)) cmb.setValue(actual);
    }

    private void log(String msg) {
        if (logOperaciones != null) logOperaciones.appendText(msg + "\n");
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtCodigo.clear();
        txtCategoria.clear();
        txtFecha.clear();
        txtMarca.clear();
        txtPrecio.clear();
        txtStock.clear();
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(200);
        return tf;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-weight: bold;");
        l.setPrefWidth(90);
        return l;
    }

    private Button btnBusqueda(String texto, String color) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color: " + color + ";" + "-fx-text-fill: white; -fx-padding: 5 10;" + "-fx-font-size: 11px;");
        return b;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}