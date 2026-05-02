package ui.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PanelExportacion extends VBox {

    public interface AccionExportar {
        boolean exportar(String ruta, String formato);
    }

    public interface AccionExportarDot {
        String generarDot();
    }

    private AccionExportar    accionImagen;
    private AccionExportarDot accionDot;
    private Label             lblEstado;

    public PanelExportacion(String nombreEstructura,
                            AccionExportarDot dotFn,
                            AccionExportar imagenFn) {
        this.accionDot    = dotFn;
        this.accionImagen = imagenFn;

        this.setSpacing(8);
        this.setPadding(new Insets(8, 0, 8, 0));
        this.setStyle(
                "-fx-background-color: #ecf0f1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8;"
        );

        Label titulo = new Label("Exportar " + nombreEstructura);
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Button btnDot = new Button("💾 Exportar .dot");
        Button btnPng = new Button("🖼 Exportar PNG");
        Button btnJpg = new Button("🖼 Exportar JPG");

        btnDot.setStyle(
                "-fx-background-color: #2c3e50;" +
                        "-fx-text-fill: white; -fx-padding: 5 12;"
        );
        btnPng.setStyle(
                "-fx-background-color: #2980b9;" +
                        "-fx-text-fill: white; -fx-padding: 5 12;"
        );
        btnJpg.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white; -fx-padding: 5 12;"
        );

        lblEstado = new Label("");
        lblEstado.setStyle("-fx-font-size: 11px;");

        btnDot.setOnAction(e -> exportarDot());
        btnPng.setOnAction(e -> exportarImagen("png"));
        btnJpg.setOnAction(e -> exportarImagen("jpg"));

        HBox botones = new HBox(8,
                btnDot, btnPng, btnJpg, lblEstado
        );

        this.getChildren().addAll(titulo, botones);
    }

    private void exportarDot() {
        if (accionDot == null) return;
        String contenido = accionDot.generarDot();
        if (contenido == null || contenido.isEmpty()) {
            setEstado("Estructura vacía", false);
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar archivo .dot");
        fc.setInitialFileName("estructura.dot");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Graphviz DOT", "*.dot"
                )
        );
        java.io.File f = fc.showSaveDialog(new Stage());
        if (f == null) return;

        boolean ok = utils.ExportarEstructuras
                .guardarDot(contenido, f.getAbsolutePath());
        setEstado(ok
                ? ".dot guardado: " + f.getName()
                : "Error al guardar", ok);
    }

    private void exportarImagen(String formato) {
        if (accionImagen == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar imagen");
        fc.setInitialFileName(
                "estructura." + formato
        );
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        formato.toUpperCase(),
                        "*." + formato
                )
        );
        java.io.File f = fc.showSaveDialog(new Stage());
        if (f == null) return;

        setEstado("⏳ Generando imagen...", true);
        boolean ok = accionImagen.exportar(
                f.getAbsolutePath(), formato
        );
        setEstado(ok
                ? "Imagen guardada: " + f.getName()
                : "Error al exportar imagen", ok);
    }

    private void setEstado(String msg, boolean ok) {
        lblEstado.setText(msg);
        lblEstado.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: " +
                        (ok ? "#27ae60" : "#e74c3c") + ";"
        );
    }
}