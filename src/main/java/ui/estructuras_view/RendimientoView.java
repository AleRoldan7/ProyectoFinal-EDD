package ui.estructuras_view;

import clases.Productos;
import clases.Sucursal;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.view.AppState;

import java.util.List;

public class RendimientoView extends VBox {

    private AppState state;
    private TextArea salida;


    public RendimientoView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Análisis de Rendimiento (Big-O)");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Nombre o código a buscar");
        txtBuscar.setPrefWidth(300);

        Button btnComparar = new Button("Comparar búsquedas");
        btnComparar.setStyle(
                "-fx-background-color: #8e44ad; -fx-text-fill: white;"
        );

        salida = new TextArea();
        salida.setEditable(false);
        salida.setPrefHeight(400);
        salida.setStyle("-fx-font-family: monospace;");

        btnComparar.setOnAction(e -> {
            String termino = txtBuscar.getText().trim();
            if (termino.isEmpty()) {
                salida.appendText("❌ Escribe un término\n");
                return;
            }
            compararBusquedas(termino);
        });

        HBox controles = new HBox(10, txtBuscar, btnComparar);

        this.getChildren().addAll(
                titulo, new Separator(),
                controles,
                new Label("Resultados:"),
                salida
        );
    }

    private void compararBusquedas(String termino) {
        salida.clear();
        salida.appendText("═══ Comparación de búsquedas: \""
                + termino + "\" ═══\n\n");

        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            salida.appendText("── Sucursal: "
                    + s.getNameSucursal() + " ──\n");

            // 1. Búsqueda secuencial (Lista enlazada)
            long t1 = System.nanoTime();
            Productos r1 = s.getLista().buscar(
                    p -> p.getName()
                            .equalsIgnoreCase(termino)
            );
            long t1fin = System.nanoTime() - t1;

            // 2. Búsqueda AVL
            long t2 = System.nanoTime();
            Productos r2 = s.buscarPorCodigo(termino);
            long t2fin = System.nanoTime() - t2;

            // 3. Búsqueda Hash (por código)
            long t3 = System.nanoTime();
            Productos r3 = s.buscarPorCodigo(termino);
            long t3fin = System.nanoTime() - t3;

            salida.appendText(String.format(
                    "  Lista (O(n)):  %,d ns  → %s\n",
                    t1fin, r1 != null ? "encontrado" : "no encontrado"
            ));
            salida.appendText(String.format(
                    "  AVL (O(logn)): %,d ns  → %s\n",
                    t2fin, r2 != null ? "encontrado" : "no encontrado"
            ));
            salida.appendText(String.format(
                    "  Hash (O(1)):   %,d ns  → %s\n\n",
                    t3fin, r3 != null ? "encontrado" : "no encontrado"
            ));
        }

        salida.appendText("═══ Complejidades teóricas ═══\n");
        salida.appendText("Lista enlazada: O(n)      — recorre todo\n");
        salida.appendText("Árbol AVL:      O(log n)  — divide el espacio\n");
        salida.appendText("Tabla Hash:     O(1) prom — acceso directo\n");
        salida.appendText("Árbol B:        O(log n)  — rango de fechas\n");
        salida.appendText("Árbol B+:       O(log n)  — búsqueda categoría\n");
    }
}
