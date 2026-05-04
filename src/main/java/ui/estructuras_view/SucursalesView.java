package ui.estructuras_view;

import clases.Sucursal;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.view.AppState;

public class SucursalesView extends VBox {

    private AppState state;
    private TextArea salida;

    public SucursalesView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Gestión de Sucursales");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        TextField txtId = new TextField();
        TextField txtNombre = new TextField();
        TextField txtUbicacion = new TextField();
        TextField txtIngreso = new TextField();
        TextField txtTraspaso = new TextField();
        TextField txtDespacho = new TextField();

        txtId.setPromptText("ID único");
        txtNombre.setPromptText("Nombre sucursal");
        txtUbicacion.setPromptText("Ciudad ");
        txtIngreso.setPromptText("Segundos");
        txtTraspaso.setPromptText("Segundos");
        txtDespacho.setPromptText("Segundos");

        form.addRow(0, new Label("ID:"), txtId);
        form.addRow(1, new Label("Nombre:"), txtNombre);
        form.addRow(2, new Label("Ubicación:"), txtUbicacion);
        form.addRow(3, new Label("T. ingreso:"), txtIngreso);
        form.addRow(4, new Label("T. traspaso:"), txtTraspaso);
        form.addRow(5, new Label("T. despacho:"), txtDespacho);

        Button btnAgregar = new Button("Agregar sucursal");
        Button btnListar = new Button("Listar sucursales");
        Button btnDetalle = new Button("Ver detalle");

        btnAgregar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        salida = new TextArea();
        salida.setEditable(false);
        salida.setPrefHeight(300);
        salida.setStyle("-fx-font-family: monospace;");

        btnAgregar.setOnAction(e -> {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                String nom = txtNombre.getText().trim();
                String ubi = txtUbicacion.getText().trim();
                int tIng = Integer.parseInt(txtIngreso.getText().trim());
                int tTra = Integer.parseInt(txtTraspaso.getText().trim());
                int tDes = Integer.parseInt(txtDespacho.getText().trim());

                Sucursal sucursal = new Sucursal(id, nom, ubi, tIng, tTra, tDes);

                boolean ok = state.getCargaCSV().agregarSucursal(sucursal);

                if (ok) {
                    state.getGrafo().agregarSucursal(id);
                    salida.appendText("Sucursal agregada correctamente\n");
                } else {
                    salida.appendText("Error: Sucursal ya existe con el ID" + id );
                }

                txtId.clear();
                txtNombre.clear();
                txtUbicacion.clear();
                txtIngreso.clear();
                txtTraspaso.clear();
                txtDespacho.clear();

            } catch (NumberFormatException ex) {
                salida.appendText("ID y tiempos deben ser números\n");
            }
        });

        btnListar.setOnAction(e -> {
            salida.clear();
            salida.appendText("Sucursales registradas:\n\n");
            for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
                salida.appendText(String.format("ID: %-4d | %-20s | %-18s\n" + "T.Ingreso: %ds | T.Traspaso: %ds | T.Despacho: %ds\n" + "         Productos: %d\n\n", s.getIdSucursal(), s.getNameSucursal(), s.getLocation(), s.getEntryTime(), s.getTransferTime(), s.getDispatchInterval(), s.getLista().size()));
            }
        });

        btnDetalle.setOnAction(e -> {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                salida.appendText("Escribe un ID para ver detalle\n");
                return;
            }
            try {
                int id = Integer.parseInt(idStr);
                Sucursal s = state.getCargaCSV().buscarSucursal(id);
                if (s == null) {
                    salida.appendText("Sucursal no encontrada\n");
                    return;
                }
                salida.clear();
                salida.appendText("Sucursales");
                salida.appendText("ID:          " + s.getIdSucursal() + "\n");
                salida.appendText("Nombre:      " + s.getNameSucursal() + "\n");
                salida.appendText("Ubicación:   " + s.getLocation() + "\n");
                salida.appendText("T. ingreso:  " + s.getEntryTime() + "s\n");
                salida.appendText("T. traspaso: " + s.getTransferTime() + "s\n");
                salida.appendText("T. despacho: " + s.getDispatchInterval() + "s\n");
                salida.appendText("Productos:   " + s.getLista().size() + "\n");
                salida.appendText("Cola ingreso:" + s.getColaIngreso().size() + "\n");
                salida.appendText("Cola salida: " + s.getColaSalida().size() + "\n");
            } catch (NumberFormatException ex) {
                salida.appendText("ID debe ser número\n");
            }
        });

        HBox botones = new HBox(8, btnAgregar, btnListar, btnDetalle);

        this.getChildren().addAll(titulo, new Separator(), form, botones, new Label("Resultado:"), salida);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}