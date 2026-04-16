package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProductosView extends VBox {

    public ProductosView() {

        this.setSpacing(10);
        this.setPadding(new Insets(10));

        Label titulo = new Label("Gestión de Productos");

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre");

        TextField codigo = new TextField();
        codigo.setPromptText("Código");

        TextField categoria = new TextField();
        categoria.setPromptText("Categoría");

        TextField precio = new TextField();
        precio.setPromptText("Precio");

        Button agregar = new Button("Agregar");
        Button buscar = new Button("Buscar");
        Button eliminar = new Button("Eliminar");

        TextArea salida = new TextArea();
        salida.setPrefHeight(200);

        agregar.setOnAction(e -> {
            salida.appendText("Producto agregado\n");

        });

        buscar.setOnAction(e -> {
            salida.appendText("Buscando producto...\n");
        });

        eliminar.setOnAction(e -> {
            salida.appendText("Producto eliminado\n");
        });

        HBox botones = new HBox(10, agregar, buscar, eliminar);

        this.getChildren().addAll(
                titulo,
                nombre, codigo, categoria, precio,
                botones,
                salida
        );
    }
}
