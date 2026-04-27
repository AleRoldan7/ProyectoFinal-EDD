package ui.estructuras_view;

import clases.Sucursal;

import estructuras.grafos.Grafo;
import estructuras.grafos.NodoArista;
import estructuras.grafos.ResultadoRuta;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import ui.view.AppState;

public class GrafoView extends VBox {

    private AppState state;
    private Canvas   canvas;
    private TextArea salida;
    private int[]    rutaActual;
    private double[] posX;
    private double[] posY;

    public GrafoView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Red de Sucursales y Rutas");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtOrigen  = new TextField();
        TextField txtDestino = new TextField();
        txtOrigen.setPromptText("ID sucursal origen");
        txtDestino.setPromptText("ID sucursal destino");
        txtOrigen.setPrefWidth(150);
        txtDestino.setPrefWidth(150);

        ToggleGroup grupo      = new ToggleGroup();
        RadioButton rbTiempo  = new RadioButton("Mínimo tiempo");
        RadioButton rbCosto   = new RadioButton("Menor costo");
        rbTiempo.setToggleGroup(grupo);
        rbCosto.setToggleGroup(grupo);
        rbTiempo.setSelected(true);

        Button btnCalcular = new Button("Calcular ruta");
        btnCalcular.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white;"
        );

        HBox controles = new HBox(10,
                new Label("Origen:"), txtOrigen,
                new Label("Destino:"), txtDestino,
                rbTiempo, rbCosto,
                btnCalcular
        );
        controles.setPadding(new Insets(5));

        canvas = new Canvas(700, 400);
        canvas.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");

        salida = new TextArea();
        salida.setEditable(false);
        salida.setPrefHeight(120);
        salida.setStyle("-fx-font-family: monospace;");

        Button btnDibujar = new Button("🔄 Redibujar grafo");
        btnDibujar.setOnAction(e -> {
            rutaActual = null;
            dibujarGrafo();
        });

        btnCalcular.setOnAction(e -> {
            try {
                int origen  = Integer.parseInt(txtOrigen.getText().trim());
                int destino = Integer.parseInt(txtDestino.getText().trim());
                boolean usarTiempo = rbTiempo.isSelected();

                ResultadoRuta resultado = state.getGrafo()
                        .determinaDijkstra(origen, destino, usarTiempo);

                if (!resultado.tieneRuta()) {
                    salida.appendText("No existe ruta entre "
                            + origen + " y " + destino + "\n");
                    return;
                }

                rutaActual = resultado.getRuta();
                dibujarGrafo();

                String criterio = usarTiempo ? "tiempo" : "costo";
                salida.appendText("Ruta óptima por " + criterio + ":\n   " + resultado + "\n");

            } catch (NumberFormatException ex) {
                salida.appendText("Los IDs deben ser números\n");
            }
        });

        HBox botones = new HBox(8, btnDibujar);

        this.getChildren().addAll(
                titulo, new Separator(),
                controles,
                canvas,
                botones,
                salida
        );
        dibujarGrafo();
    }


    private void dibujarGrafo() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Grafo grafo = state.getGrafo();
        int total   = grafo.getTotalNodos();
        if (total == 0) {
            gc.setFill(Color.GRAY);
            gc.fillText("No hay sucursales cargadas", 300, 200);
            return;
        }

        calcularPosiciones(total);

        for (int i = 0; i < total; i++) {
            int idOrigen = grafo.getIdSucursal(i);
            NodoArista arista = grafo.getAristas(i);

            while (arista != null) {
                int j = buscarIndicePorId(grafo, arista.getDestino());
                if (j != -1 && j > i) {
                    boolean enRuta = estaEnRuta(idOrigen, arista.getDestino());

                    gc.setStroke(enRuta ? Color.web("#e74c3c")
                            : Color.web("#95a5a6"));
                    gc.setLineWidth(enRuta ? 3 : 1.5);
                    gc.strokeLine(posX[i], posY[i], posX[j], posY[j]);

                    double mx = (posX[i] + posX[j]) / 2;
                    double my = (posY[i] + posY[j]) / 2;
                    gc.setFill(Color.web("#2c3e50"));
                    gc.setFont(javafx.scene.text.Font.font(10));
                    gc.fillText(String.format("T:%.0f C:%.0f",
                            arista.getTiempo(), arista.getCosto()), mx, my);
                }
                arista = arista.getSiguiente();
            }
        }

        for (int i = 0; i < total; i++) {
            int idSuc = grafo.getIdSucursal(i);
            boolean enRuta = estaEnRutaNodo(idSuc);

            gc.setFill(enRuta ? Color.web("#e74c3c")
                    : Color.web("#2980b9"));
            gc.fillOval(posX[i] - 22, posY[i] - 22, 44, 44);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(posX[i] - 22, posY[i] - 22, 44, 44);

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font(13));
            gc.fillText(String.valueOf(idSuc),
                    posX[i] - 6, posY[i] + 5);

            Sucursal s = state.getCargaCSV().buscarSucursal(idSuc);
            if (s != null) {
                gc.setFill(Color.web("#2c3e50"));
                gc.setFont(javafx.scene.text.Font.font(11));
                String nom = s.getNameSucursal();
                if (nom.length() > 12) nom = nom.substring(0, 12) + "..";
                gc.fillText(nom, posX[i] - 30, posY[i] + 38);
            }
        }
    }

    private void calcularPosiciones(int total) {
        posX = new double[total];
        posY = new double[total];
        double cx = canvas.getWidth()  / 2;
        double cy = canvas.getHeight() / 2;
        double r  = Math.min(cx, cy) - 60;

        for (int i = 0; i < total; i++) {
            double angulo = 2 * Math.PI * i / total - Math.PI / 2;
            posX[i] = cx + r * Math.cos(angulo);
            posY[i] = cy + r * Math.sin(angulo);
        }
    }

    private int buscarIndicePorId(Grafo grafo, int id) {
        for (int i = 0; i < grafo.getTotalNodos(); i++) {
            if (grafo.getIdSucursal(i) == id) return i;
        }
        return -1;
    }

    private boolean estaEnRuta(int a, int b) {
        if (rutaActual == null) return false;
        for (int i = 0; i < rutaActual.length - 1; i++) {
            if ((rutaActual[i] == a && rutaActual[i+1] == b) ||
                    (rutaActual[i] == b && rutaActual[i+1] == a))
                return true;
        }
        return false;
    }

    private boolean estaEnRutaNodo(int id) {
        if (rutaActual == null) return false;
        for (int nodo : rutaActual) {
            if (nodo == id) return true;
        }
        return false;
    }
}