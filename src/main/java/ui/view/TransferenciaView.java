package ui.view;

import clases.Productos;
import clases.Sucursal;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import transferencia.PasoTransferencia;
import transferencia.ResultadoTransferencia;
import transferencia.TransfereciaProductos;

import java.util.List;

public class TransferenciaView extends VBox {

    private AppState state;
    private TransfereciaProductos transfereciaProductos;

    private ComboBox<String> cmbOrigen;
    private ComboBox<String> cmbDestino;
    private ComboBox<String> cmbProducto;
    private RadioButton rbTiempo, rbCosto;
    private TextArea logTransferencia;
    private TableView<PasoTransferencia> tabla;
    //private

    private Canvas canvasEstado;

    private Timeline animacion;
    private List<PasoTransferencia> pasos;
    private int pasoActual;
    private int[] rutaAnimacion;

    private double[] posX, posY;
    private static final int RADIO = 28;
    private static final int CANVAS_W = 1000;
    private static final int CANVAS_H = 500;

    public TransferenciaView(AppState state) {
        this.state = state;
        this.transfereciaProductos = new TransfereciaProductos(state);
        this.setSpacing(0);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(crearTabTransferir(), crearTabHistorial());

        this.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    private Tab crearTabTransferir() {
        Tab tab = new Tab("Transferir Producto");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Transferencia entre sucursales");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        cmbOrigen = new ComboBox<>();
        cmbOrigen.setPromptText("Sucursal origen");
        cmbOrigen.setPrefWidth(260);
        recargarSucursales(cmbOrigen);

        cmbProducto = new ComboBox<>();
        cmbProducto.setPromptText("Selecciona origen primero");
        cmbProducto.setPrefWidth(320);

        cmbOrigen.setOnAction(e -> cargarProductosDeOrigen());

        cmbDestino = new ComboBox<>();
        cmbDestino.setPromptText("Sucursal destino");
        cmbDestino.setPrefWidth(260);
        recargarSucursales(cmbDestino);

        ToggleGroup grupo = new ToggleGroup();
        rbTiempo = new RadioButton("Mínimo tiempo");
        rbCosto = new RadioButton("Menor costo");
        rbTiempo.setToggleGroup(grupo);
        rbCosto.setToggleGroup(grupo);
        rbTiempo.setSelected(true);

        Label lblVel = new Label("Velocidad animación:");
        Slider sldVel = new Slider(300, 2500, 1000);
        sldVel.setPrefWidth(150);
        sldVel.setShowTickLabels(true);
        sldVel.setMajorTickUnit(1000);

        Button btnTransferir = new Button("▶ Transferir");
        Button btnPausar = new Button("⏸ Pausar");
        Button btnReset = new Button("↺ Reset");

        btnTransferir.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;" + "-fx-font-size: 13px; -fx-padding: 8 20;");
        btnPausar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;" + "-fx-padding: 8 14;");
        btnReset.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;" + "-fx-padding: 8 14;");

        canvasEstado = new Canvas(CANVAS_W, CANVAS_H);
        canvasEstado.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");

        /*
        logTransferencia = new TextArea();
        logTransferencia.setEditable(false);
        logTransferencia.setPrefHeight(180);
        logTransferencia.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
         */

        tabla = crearTablaLog();
        tabla.setPrefHeight(180);

        HBox leyenda = new HBox(15, itemLeyenda("#2980b9", "Sin visitar"), itemLeyenda("#f39c12", "Procesando"), itemLeyenda("#e74c3c", "Ya procesado"), itemLeyenda("#27ae60", "Destino ✅"));

        btnTransferir.setOnAction(e -> {
            iniciarTransferencia(sldVel.getValue());
        });

        btnPausar.setOnAction(e -> {
            if (animacion != null) {
                if (animacion.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                    animacion.pause();
                    btnPausar.setText("▶ Reanudar");
                } else {
                    animacion.play();
                    btnPausar.setText("⏸ Pausar");
                }
            }
        });

        btnReset.setOnAction(e -> {
            if (animacion != null) animacion.stop();
            pasoActual = 0;
            pasos = null;
            rutaAnimacion = null;
            logTransferencia.clear();
            btnPausar.setText("⏸ Pausar");
            dibujarGrafoBase(null, -1);
        });

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(10);

        form.addRow(0, etiqueta("Origen:"), cmbOrigen, etiqueta("Producto:"), cmbProducto);
        form.addRow(1, etiqueta("Destino:"), cmbDestino, new HBox(15, rbTiempo, rbCosto));
        form.addRow(2, lblVel, sldVel);

        HBox botones = new HBox(10, btnTransferir, btnPausar, btnReset);

        root.getChildren().addAll(titulo, new Separator(), form, botones, leyenda, canvasEstado,
                new Label("Log de transferencia:"), tabla);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);

        dibujarGrafoBase(null, -1);
        return tab;
    }


    private Tab crearTabHistorial() {
        Tab tab = new Tab("📊 Estado de Sucursales");
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label titulo = new Label("Estado de colas por sucursal");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;" + "-fx-padding: 6 14;");

        TextArea txtEstado = new TextArea();
        txtEstado.setEditable(false);
        txtEstado.setPrefHeight(500);
        txtEstado.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        TableView<Sucursal> tablaSucursal = crearTablaSucursales();
        btnRefrescar.setOnAction(e -> {

            tablaSucursal.setStyle(" -fx-background-color: #ffffff;" + "" +
                            "-fx-border-color: #dcdde1;\n" +
                            "-fx-border-radius: 6; ");
            tablaSucursal.getItems().clear();
            tablaSucursal.getItems().addAll(state.getCargaCSV().getListaSucursales());
            /*
            txtEstado.clear();
            txtEstado.appendText("Estado Sucursales");

            for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
                txtEstado.appendText(String.format("%s (ID: %d)\n", s.getNameSucursal(), s.getIdSucursal()));
                txtEstado.appendText(String.format("Productos en inventario: %d\n", s.getLista().size()));
                txtEstado.appendText(String.format("Cola ingreso:  %d productos\n", s.getColaIngreso().size()));
                txtEstado.appendText(String.format("Cola traspaso: %d productos\n", s.getColaTraspaso().size()));
                txtEstado.appendText(String.format("Cola salida:   %d productos\n", s.getColaSalida().size()));
                txtEstado.appendText(String.format("T.Ingreso: %ds | T.Traspaso: %ds" + " | T.Despacho: %ds\n", s.getEntryTime(), s.getTransferTime(), s.getDispatchInterval()));

                List<Productos> prods = s.getLista().toList();
                if (!prods.isEmpty()) {
                    txtEstado.appendText("   Inventario:\n");
                    for (Productos p : prods) {
                        txtEstado.appendText(String.format("   • %-25s | %s | Q%.2f | %s\n", p.getName(), p.getBarCode(), p.getPrice(), p.getStatus()));
                    }
                }
                txtEstado.appendText("\n");
            }
             */
        });

        root.getChildren().addAll(titulo, new Separator(), btnRefrescar, tablaSucursal);

        tab.setContent(new ScrollPane(root) {{
            setFitToWidth(true);
        }});
        return tab;
    }

    private void iniciarTransferencia(double velocidadMs) {
        if (animacion != null) animacion.stop();

        String selOrigen = cmbOrigen.getValue();
        String selDestino = cmbDestino.getValue();
        String selProd = cmbProducto.getValue();

        if (selOrigen == null || selDestino == null || selProd == null) {
            log("Selecciona origen, destino y producto");
            return;
        }

        int idOrigen = Integer.parseInt(selOrigen.split(" - ")[0].trim());
        int idDestino = Integer.parseInt(selDestino.split(" - ")[0].trim());


        String codigo = selProd.split("\\|")[0].trim();

        boolean usarTiempo = rbTiempo.isSelected();

        ResultadoTransferencia res = transfereciaProductos.transferencia(codigo, idOrigen, idDestino, usarTiempo);

        //logTransferencia.clear();
        //log(res.getMensaje());
        tabla.getItems().clear();


        if (!res.isExito()) return;

        pasos = res.getPasos();
        rutaAnimacion = res.getRuta();
        pasoActual = 0;

        dibujarGrafoBase(rutaAnimacion, -1);

        log(String.format("Ruta: %d sucursales | ETA: %.0fs (%.1f min)\n", rutaAnimacion.length, res.getTotalSegundos(), res.getTotalSegundos() / 60.0));

        animacion = new Timeline(new KeyFrame(Duration.millis(velocidadMs), e -> animarPaso()));

        animacion.setCycleCount(pasos.size());

        animacion.setOnFinished(e -> {
            log("\nTransferencia completada");
            cargarProductosDeOrigen();
        });

        animacion.play();
    }

    private void animarPaso() {
        if (pasos == null || pasoActual >= pasos.size()) return;

        PasoTransferencia paso = pasos.get(pasoActual);

        //log(paso.getMensaje());
        tabla.getItems().add(paso);
        dibujarGrafoBase(rutaAnimacion, paso.getSucursaleID());
        pasoActual++;
    }

    private void dibujarGrafoBase(int[] ruta, int sucursalActiva) {
        GraphicsContext gc = canvasEstado.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);

        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        int total = state.getGrafo().getTotalNodos();
        if (total == 0) {
            gc.setFill(Color.GRAY);
            gc.fillText("Carga sucursales primero", 300, 190);
            return;
        }

        calcularPosiciones(total);

        // Dibujar aristas
        for (int i = 0; i < total; i++) {
            int idO = state.getGrafo().getIdSucursal(i);
            estructuras.grafos.NodoArista a = state.getGrafo().getAristas(i);

            while (a != null) {
                int j = buscarIdx(a.getDestino());
                if (j != -1 && j > i) {
                    boolean enRuta = enRuta(idO, a.getDestino(), ruta);

                    gc.setStroke(enRuta ? Color.web("#e74c3c") : Color.web("#c8d6e5"));
                    gc.setLineWidth(enRuta ? 3 : 1.5);
                    gc.strokeLine(posX[i], posY[i], posX[j], posY[j]);

                    // Peso
                    double mx = (posX[i] + posX[j]) / 2;
                    double my = (posY[i] + posY[j]) / 2;
                    gc.setFill(Color.web("#ffffffcc"));
                    gc.fillRoundRect(mx - 18, my - 9, 60, 13, 3, 3);
                    gc.setFill(enRuta ? Color.web("#c0392b") : Color.web("#7f8c8d"));
                    gc.setFont(Font.font(8));
                    gc.fillText(String.format("T:%.0f C:%.0f", a.getTiempo(), a.getCosto()), mx - 16, my);
                }
                a = a.getSiguiente();
            }
        }

        for (int i = 0; i < total; i++) {
            int id = state.getGrafo().getIdSucursal(i);
            dibujarNodo(gc, i, id, ruta, sucursalActiva);
        }
    }

    private void dibujarNodo(GraphicsContext gc, int idx, int idSuc, int[] ruta, int sucursalActiva) {
        double x = posX[idx];
        double y = posY[idx];

        Color color;
        if (idSuc == sucursalActiva) {
            color = Color.web("#f39c12");
        } else if (ruta != null && esDestino(idSuc, ruta) && pasoActual >= pasos.size()) {
            color = Color.web("#27ae60");
        } else if (enRutaNodo(idSuc, ruta)) {
            color = Color.web("#e74c3c");
        } else {
            color = Color.web("#2980b9");
        }

        gc.setFill(Color.web("#00000020"));
        gc.fillOval(x - RADIO + 3, y - RADIO + 3, RADIO * 2, RADIO * 2);

        gc.setFill(color);
        gc.fillOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String ids = String.valueOf(idSuc);
        gc.fillText(ids, x - ids.length() * 4.5, y + 5);

        Sucursal s = state.getCargaCSV().buscarSucursal(idSuc);
        if (s != null) {
            String nom = s.getNameSucursal();
            if (nom.length() > 10) nom = nom.substring(0, 9) + "..";

            String info = String.format("%s | %d prods", nom, s.getLista().size());
            double tw = info.length() * 5.5;

            gc.setFill(Color.web("#ffffffdd"));
            gc.fillRoundRect(x - tw / 2 - 3, y + RADIO + 2, tw + 6, 27, 4, 4);

            gc.setFill(Color.web("#2c3e50"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            gc.fillText(nom, x - tw / 2, y + RADIO + 12);

            gc.setFill(Color.web("#7f8c8d"));
            gc.setFont(Font.font("Arial", 9));
            gc.fillText(s.getLista().size() + " productos", x - tw / 2, y + RADIO + 23);
        }
    }

    private void cargarProductosDeOrigen() {
        cmbProducto.getItems().clear();
        String sel = cmbOrigen.getValue();
        if (sel == null) return;

        int id = Integer.parseInt(sel.split(" - ")[0].trim());
        Sucursal s = state.getCargaCSV().buscarSucursal(id);
        if (s == null) return;

        for (Productos p : s.getLista().toList()) {
            cmbProducto.getItems().add(p.getBarCode() + " | " + p.getName());
        }

        if (cmbProducto.getItems().isEmpty()) {
            cmbProducto.setPromptText("Sin productos");
        } else {
            cmbProducto.setPromptText(cmbProducto.getItems().size() + " disponibles");
        }
    }

    private void calcularPosiciones(int total) {
        posX = new double[total];
        posY = new double[total];
        double cx = CANVAS_W / 2.0;
        double cy = CANVAS_H / 2.0;
        double r = Math.min(cx, cy) - 40;

        for (int i = 0; i < total; i++) {
            double ang = 2 * Math.PI * i / total - Math.PI / 2;
            posX[i] = cx + r * Math.cos(ang);
            posY[i] = cy + r * Math.sin(ang);
        }
    }

    private int buscarIdx(int id) {
        for (int i = 0; i < state.getGrafo().getTotalNodos(); i++) {
            if (state.getGrafo().getIdSucursal(i) == id) return i;
        }
        return -1;
    }

    private boolean enRuta(int a, int b, int[] ruta) {
        if (ruta == null) return false;
        for (int i = 0; i < ruta.length - 1; i++) {
            if ((ruta[i] == a && ruta[i + 1] == b) || (ruta[i] == b && ruta[i + 1] == a)) return true;
        }
        return false;
    }

    private boolean enRutaNodo(int id, int[] ruta) {
        if (ruta == null) return false;
        for (int n : ruta) if (n == id) return true;
        return false;
    }

    private boolean esDestino(int id, int[] ruta) {
        if (ruta == null || ruta.length == 0) return false;
        return id == ruta[ruta.length - 1];
    }

    private void recargarSucursales(ComboBox<String> cmb) {
        cmb.getItems().clear();
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmb.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }
    }

    private void log(String msg) {
        tabla.getItems().add(new PasoTransferencia(0,"Sistema", "Informacion",0, msg));
        //logTransferencia.appendText(msg + "\n");
    }

    private HBox itemLeyenda(String hex, String texto) {
        Canvas c = new Canvas(12, 12);
        c.getGraphicsContext2D().setFill(Color.web(hex));
        c.getGraphicsContext2D().fillOval(0, 0, 12, 12);
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 10px;");
        return new HBox(4, c, l);
    }

    private Label etiqueta(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight: bold;");
        l.setPrefWidth(80);
        return l;
    }

    private TableView<PasoTransferencia> crearTablaLog() {
        TableView<PasoTransferencia> tabla = new TableView<>();

        tabla.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dcdde1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-font-size: 11px;"
        );
        TableColumn<PasoTransferencia, String> colSucursal =
                new TableColumn<>("Sucursal");
        colSucursal.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getNombreSucursal()
                ));

        TableColumn<PasoTransferencia, String> colEtapa =
                new TableColumn<>("Etapa");
        colEtapa.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getEtapa()
                ));

        TableColumn<PasoTransferencia, String> colTiempo =
                new TableColumn<>("Tiempo");
        colTiempo.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getTiempo() + " ms"
                ));

        TableColumn<PasoTransferencia, String> colMensaje =
                new TableColumn<>("Detalle");
        colMensaje.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getMensaje()
                ));

        tabla.getColumns().addAll(colSucursal, colEtapa, colTiempo, colMensaje);

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tabla;
    }

    private TableView<Sucursal> crearTablaSucursales() {
        TableView<Sucursal> tabla = new TableView<>();

        TableColumn<Sucursal, String> colNombre =
                new TableColumn<>("Sucursal");
        colNombre.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getNameSucursal()
                ));

        TableColumn<Sucursal, String> colInventario =
                new TableColumn<>("Inventario");
        colInventario.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(d.getValue().getLista().size())
                ));

        TableColumn<Sucursal, String> colIngreso =
                new TableColumn<>("Cola Ingreso");
        colIngreso.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(d.getValue().getColaIngreso().size())
                ));

        TableColumn<Sucursal, String> colTraspaso =
                new TableColumn<>("Cola Traspaso");
        colTraspaso.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(d.getValue().getColaTraspaso().size())
                ));

        TableColumn<Sucursal, String> colSalida =
                new TableColumn<>("Cola Salida");
        colSalida.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(d.getValue().getColaSalida().size())
                ));

        tabla.getColumns().addAll(
                colNombre, colInventario,
                colIngreso, colTraspaso, colSalida
        );

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tabla;
    }
}