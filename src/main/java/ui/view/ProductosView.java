package ui.view;

import clases.Productos;
import clases.Sucursal;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.view.AppState;

import java.util.List;

public class ProductosView extends VBox {

    private AppState state;
    private TextArea  salida;
    private TextField txtNombre, txtCodigo, txtCategoria,
            txtFecha, txtMarca, txtPrecio, txtStock,
            txtSucursal;

    public ProductosView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Gestión de Productos");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Formulario en grid
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        txtNombre    = campo("Nombre del producto");
        txtCodigo    = campo("Código de barras");
        txtCategoria = campo("Categoría");
        txtFecha     = campo("Fecha caducidad (YYYY-MM-DD)");
        txtMarca     = campo("Marca");
        txtPrecio    = campo("Precio");
        txtStock     = campo("Stock");
        txtSucursal  = campo("ID Sucursal");

        form.addRow(0, new Label("Nombre:"),    txtNombre);
        form.addRow(1, new Label("Código:"),    txtCodigo);
        form.addRow(2, new Label("Categoría:"), txtCategoria);
        form.addRow(3, new Label("Fecha cad:"), txtFecha);
        form.addRow(4, new Label("Marca:"),     txtMarca);
        form.addRow(5, new Label("Precio:"),    txtPrecio);
        form.addRow(6, new Label("Stock:"),     txtStock);
        form.addRow(7, new Label("Sucursal:"),  txtSucursal);

        // Botones principales
        Button btnAgregar  = new Button("Agregar");
        Button btnEliminar = new Button("Eliminar por código");
        Button btnLimpiar  = new Button("Limpiar");

        btnAgregar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        // Botones de búsqueda
        Button btnBuscarNombre    = new Button("Buscar por nombre (AVL)");
        Button btnBuscarCodigo    = new Button("Buscar por código (Hash)");
        Button btnBuscarCategoria = new Button("Buscar por categoría (B+)");
        Button btnBuscarFecha     = new Button("Buscar por fecha (B)");
        Button btnListarTodos     = new Button("Listar todos (inOrder AVL)");
        Button btnDeshacer        = new Button("↩ Deshacer");

        btnDeshacer.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");

        salida = new TextArea();
        salida.setEditable(false);
        salida.setPrefHeight(250);
        salida.setStyle("-fx-font-family: monospace;");

        // ── Acciones ──

        btnAgregar.setOnAction(e -> agregarProducto());
        btnEliminar.setOnAction(e -> eliminarProducto());
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        btnBuscarNombre.setOnAction(e -> buscarPorNombre());
        btnBuscarCodigo.setOnAction(e -> buscarPorCodigo());
        btnBuscarCategoria.setOnAction(e -> buscarPorCategoria());
        btnBuscarFecha.setOnAction(e -> buscarPorFecha());
        btnListarTodos.setOnAction(e -> listarTodos());
        btnDeshacer.setOnAction(e -> deshacer());

        HBox botonesAccion = new HBox(8,
                btnAgregar, btnEliminar, btnDeshacer, btnLimpiar
        );
        HBox botonesBusqueda = new HBox(6,
                btnBuscarNombre, btnBuscarCodigo,
                btnBuscarCategoria, btnBuscarFecha, btnListarTodos
        );

        this.getChildren().addAll(
                titulo,
                new Separator(),
                form,
                botonesAccion,
                new Separator(),
                new Label("Búsquedas:"),
                botonesBusqueda,
                new Label("Resultado:"),
                salida
        );
    }

    // ─────────────────────────────────────────
    // ACCIONES
    // ─────────────────────────────────────────

    private void agregarProducto() {
        try {
            int    idSuc = Integer.parseInt(txtSucursal.getText().trim());
            String nom   = txtNombre.getText().trim();
            String cod   = txtCodigo.getText().trim();
            String cat   = txtCategoria.getText().trim();
            String fec   = txtFecha.getText().trim();
            String mar   = txtMarca.getText().trim();
            double pre   = Double.parseDouble(txtPrecio.getText().trim());
            int    sto   = Integer.parseInt(txtStock.getText().trim());

            if (nom.isEmpty() || cod.isEmpty()) {
                salida.appendText("❌ Nombre y código son obligatorios\n");
                return;
            }

            Sucursal suc = state.getCargaCSV().buscarSucursal(idSuc);
            if (suc == null) {
                salida.appendText("❌ Sucursal ID=" + idSuc + " no existe\n");
                return;
            }

            Productos p = new Productos(
                    idSuc, nom, cod, cat, fec, mar, pre, sto
            );
            boolean ok = suc.agregarProducto(p);
            if (ok) {
                state.getAvlGlobal().insert(p);
                salida.appendText("✅ Producto agregado: " + nom + "\n");
                limpiarFormulario();
            } else {
                salida.appendText("❌ Error al agregar (¿código duplicado?)\n");
            }
        } catch (NumberFormatException ex) {
            salida.appendText("❌ Precio, Stock y Sucursal deben ser números\n");
        }
    }

    private void eliminarProducto() {
        String cod = txtCodigo.getText().trim();
        if (cod.isEmpty()) {
            salida.appendText("❌ Ingresa el código de barras\n");
            return;
        }
        boolean eliminado = false;
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            if (s.eliminarProducto(cod)) {
                salida.appendText("✅ Eliminado de sucursal: "
                        + s.getNameSucursal() + "\n");
                eliminado = true;
                break;
            }
        }
        if (!eliminado)
            salida.appendText("❌ Código no encontrado: " + cod + "\n");
    }

    private void buscarPorNombre() {
        String nom = txtNombre.getText().trim();
        if (nom.isEmpty()) {
            salida.appendText("❌ Escribe un nombre\n");
            return;
        }
        long inicio = System.nanoTime();
        // Buscar en cada sucursal
        boolean encontrado = false;
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            Productos p = s.buscarPorCodigo(nom);
            if (p != null) {
                long tiempo = System.nanoTime() - inicio;
                salida.appendText("✅ [AVL] Encontrado en "
                        + s.getNameSucursal() + ":\n   "
                        + p + "\n   Tiempo: " + tiempo + " ns\n");
                encontrado = true;
            }
        }
        if (!encontrado)
            salida.appendText("❌ No encontrado: " + nom + "\n");
    }

    private void buscarPorCodigo() {
        String cod = txtCodigo.getText().trim();
        if (cod.isEmpty()) {
            salida.appendText("❌ Escribe un código\n");
            return;
        }
        long inicio = System.nanoTime();
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            Productos p = s.buscarPorCodigo(cod);
            if (p != null) {
                long tiempo = System.nanoTime() - inicio;
                salida.appendText("✅ [Hash] Encontrado en "
                        + s.getNameSucursal() + ":\n   "
                        + p + "\n   Tiempo: " + tiempo + " ns\n");
                return;
            }
        }
        salida.appendText("❌ No encontrado: " + cod + "\n");
    }

    private void buscarPorCategoria() {
        String cat = txtCategoria.getText().trim();
        if (cat.isEmpty()) {
            salida.appendText("❌ Escribe una categoría\n");
            return;
        }
        long inicio = System.nanoTime();
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            List<String> res = s.buscarPorCategoria(cat);
            if (!res.isEmpty()) {
                long tiempo = System.nanoTime() - inicio;
                salida.appendText("✅ [B+] " + s.getNameSucursal()
                        + " → " + res.size() + " productos\n"
                        + "   Tiempo: " + tiempo + " ns\n");
            }
        }
    }

    private void buscarPorFecha() {
        String fec = txtFecha.getText().trim();
        if (fec.isEmpty()) {
            salida.appendText("❌ Escribe una fecha (YYYY-MM-DD)\n");
            return;
        }
        // Buscar rango: misma fecha como desde y hasta
        long inicio = System.nanoTime();
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            List<String> res = s.buscarPorRangoFecha(fec, fec);
            if (!res.isEmpty()) {
                long tiempo = System.nanoTime() - inicio;
                salida.appendText("✅ [B] " + s.getNameSucursal()
                        + " → " + res.size() + " productos\n"
                        + "   Tiempo: " + tiempo + " ns\n");
            }
        }
    }

    private void listarTodos() {
        salida.clear();
        List<Productos> todos = state.getAvlGlobal().inOrder();
        salida.appendText("📋 Total productos (ordenados): "
                + todos.size() + "\n\n");
        for (Productos p : todos) {
            salida.appendText("• " + p + "\n");
        }
    }

    private void deshacer() {
        String cod = txtCodigo.getText().trim();
        if (cod.isEmpty()) {
            salida.appendText("❌ Ingresa el código para deshacer\n");
            return;
        }
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            clases.OperacionProducto op = s.deshacerUltimaOperacion();
            if (op != null) {
                salida.appendText("↩ Deshecho: " + op + "\n");
                return;
            }
        }
        salida.appendText("❌ No hay operaciones para deshacer\n");
    }

    private void limpiarFormulario() {
        txtNombre.clear(); txtCodigo.clear();
        txtCategoria.clear(); txtFecha.clear();
        txtMarca.clear(); txtPrecio.clear();
        txtStock.clear(); txtSucursal.clear();
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        return tf;
    }
}