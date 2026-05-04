package ui.view;

import clases.Sucursal;
import estructuras.grafos.NodoArista;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ConexionView extends VBox {

    private AppState state;
    private TextArea salida;

    public ConexionView(AppState state) {

        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Conexiones entre Sucursales");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<String> cmbOrigen = new ComboBox<>();
        ComboBox<String> cmbDestino = new ComboBox<>();

        cmbOrigen.setPromptText("Sucursal origen");
        cmbDestino.setPromptText("Sucursal destino");

        TextField txtTiempo = new TextField();
        txtTiempo.setPromptText("Tiempo");

        TextField txtCosto = new TextField();
        txtCosto.setPromptText("Costo");

        cargarSucursales(cmbOrigen);
        cargarSucursales(cmbDestino);

        Button btnConectar = new Button("Conectar");
        Button btnVer = new Button("Ver conexiones");

        btnConectar.setStyle("-fx-background-color:#2980b9; -fx-text-fill:white;");
        btnVer.setStyle("-fx-background-color:#7f8c8d; -fx-text-fill:white;");

        salida = new TextArea();
        salida.setEditable(false);
        salida.setPrefHeight(300);

        btnConectar.setOnAction(e -> {

            String o = cmbOrigen.getValue();
            String d = cmbDestino.getValue();

            if (o == null || d == null) {
                salida.appendText("Selecciona origen y destino\n");
                return;
            }

            try {
                int idO = Integer.parseInt(o.split(" - ")[0]);
                int idD = Integer.parseInt(d.split(" - ")[0]);
                double tiempo = Double.parseDouble(txtTiempo.getText().trim());
                double costo = Double.parseDouble(txtCosto.getText().trim());

                state.getGrafo().agregarConexion(idO, idD, tiempo, costo);

                salida.appendText("Conectado: " + idO + " -> " + idD + " (T:" + tiempo + ", C:" + costo + ")\n");

            } catch (Exception ex) {
                salida.appendText("Error en datos\n");
            }
        });

        btnVer.setOnAction(e -> {

            salida.clear();
            salida.appendText("Conexiones:\n\n");

            for (Sucursal s : state.getCargaCSV().getListaSucursales()) {

                int id = s.getIdSucursal();
                salida.appendText(state.getGrafo().obtenerConexionesComoTexto());

                NodoArista actual = state.getGrafo().getAdyacentesPorId(id);

                if (actual == null) {
                    salida.appendText("  (sin conexiones)\n\n");
                    continue;
                }

                while (actual != null) {
                    salida.appendText("  → " + actual.getDestino() + " (Tiempo: " + actual.getTiempo() + ", Costo: " + actual.getCosto() + ")\n");
                    actual = actual.getSiguiente();
                }
            }
        });

        HBox fila1 = new HBox(10, new Label("Origen:"), cmbOrigen);
        HBox fila2 = new HBox(10, new Label("Destino:"), cmbDestino);
        HBox fila3 = new HBox(10, new Label("Tiempo:"), txtTiempo);
        HBox fila4 = new HBox(10, new Label("Costo:"), txtCosto);
        HBox botones = new HBox(10, btnConectar, btnVer);

        this.getChildren().addAll(titulo, new Separator(), fila1, fila2, fila3, fila4, botones, new Label("Salida:"), salida);
    }

    private void cargarSucursales(ComboBox<String> cmb) {
        cmb.getItems().clear();

        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmb.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}