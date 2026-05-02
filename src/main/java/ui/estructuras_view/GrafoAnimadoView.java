package ui.estructuras_view;

import clases.Sucursal;
import estructuras.grafos.Grafo;
import estructuras.grafos.NodoArista;
import estructuras.grafos.ResultadoRuta;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import ui.view.AppState;

public class GrafoAnimadoView extends VBox {

    private AppState state;
    private Canvas    canvas;
    private TextArea  salida;
    private Timeline  animacion;
    private int[]     rutaFinal;
    private int       pasoActual;
    private boolean[] visitado;
    private double[]  posX, posY;

    private static final int RADIO    = 26;
    private static final int CANVAS_W = 750;
    private static final int CANVAS_H = 430;

    public GrafoAnimadoView(AppState state) {
        this.state = state;
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Grafo de Sucursales Animación Dijkstra");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtOrigen  = new TextField();
        TextField txtDestino = new TextField();
        txtOrigen.setPromptText("ID origen");
        txtDestino.setPromptText("ID destino");
        txtOrigen.setPrefWidth(90);
        txtDestino.setPrefWidth(90);

        ToggleGroup grupo    = new ToggleGroup();
        RadioButton rbTiempo = new RadioButton("Min. tiempo");
        RadioButton rbCosto  = new RadioButton("Min. costo");
        rbTiempo.setToggleGroup(grupo);
        rbCosto.setToggleGroup(grupo);
        rbTiempo.setSelected(true);

        Button btnCalcular = new Button("▶ Calcular y animar");
        Button btnPausar   = new Button("⏸ Pausar");
        Button btnReset    = new Button("↺ Reiniciar");

        btnCalcular.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-padding: 6 14;"
        );
        btnPausar.setStyle(
                "-fx-background-color: #e67e22; -fx-text-fill: white; " +
                        "-fx-padding: 6 14;"
        );
        btnReset.setStyle(
                "-fx-background-color: #7f8c8d; -fx-text-fill: white; " +
                        "-fx-padding: 6 14;"
        );

        Label lblVelocidad = new Label("Velocidad:");
        Slider sldVelocidad = new Slider(200, 2000, 800);
        sldVelocidad.setShowTickLabels(true);
        sldVelocidad.setMajorTickUnit(600);
        sldVelocidad.setPrefWidth(160);
        Label lblMs = new Label("ms/paso");

        HBox controles = new HBox(8,
                new Label("Origen:"), txtOrigen,
                new Label("Destino:"), txtDestino,
                rbTiempo, rbCosto,
                btnCalcular, btnPausar, btnReset
        );
        HBox ctrlVel = new HBox(8,
                lblVelocidad, sldVelocidad, lblMs
        );
        ctrlVel.setPadding(new Insets(0, 0, 0, 5));

        canvas = new Canvas(CANVAS_W, CANVAS_H);
        canvas.setStyle(
                "-fx-border-color: #bdc3c7; -fx-border-width: 1;"
        );

        salida = new TextArea();
        salida.setEditable(false);
        salida.setPrefHeight(110);
        salida.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        HBox leyenda = new HBox(15,
                cuadroLeyenda("#2980b9", "Sin visitar"),
                cuadroLeyenda("#f39c12", "Visitando"),
                cuadroLeyenda("#e74c3c", "En ruta óptima"),
                cuadroLeyenda("#27ae60", "Destino alcanzado")
        );
        leyenda.setPadding(new Insets(5));

        btnCalcular.setOnAction(e -> {
            try {
                int origen  = Integer.parseInt(txtOrigen.getText().trim());
                int destino = Integer.parseInt(txtDestino.getText().trim());
                boolean usarTiempo = rbTiempo.isSelected();
                double velocidad   = sldVelocidad.getValue();
                iniciarAnimacion(origen, destino, usarTiempo, velocidad);
            } catch (NumberFormatException ex) {
                salida.appendText("IDs deben ser números\n");
            }
        });

        btnPausar.setOnAction(e -> {
            if (animacion != null) {
                if (animacion.getStatus() ==
                        javafx.animation.Animation.Status.RUNNING) {
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
            rutaFinal    = null;
            pasoActual   = 0;
            visitado     = null;
            btnPausar.setText("⏸ Pausar");
            salida.clear();
            dibujarGrafoBase(null);
        });

        this.getChildren().addAll(
                titulo, new Separator(),
                controles, ctrlVel, leyenda,
                canvas, salida
        );

        dibujarGrafoBase(null);
    }

    private void iniciarAnimacion(int origen, int destino,
                                  boolean usarTiempo,
                                  double velocidadMs) {
        if (animacion != null) animacion.stop();

        Grafo grafo = state.getGrafo();
        int total   = grafo.getTotalNodos();

        if (total == 0) {
            salida.appendText("No hay sucursales en el grafo\n");
            return;
        }

        ResultadoRuta resultado = grafo.determinaDijkstra(origen, destino, usarTiempo);

        if (!resultado.tieneRuta()) {
            salida.appendText("Sin ruta entre " + origen + " → " + destino + "\n");
            return;
        }

        rutaFinal  = resultado.getRuta();
        pasoActual = 0;
        visitado   = new boolean[total];

        String criterio = usarTiempo ? "tiempo" : "costo";
        salida.appendText("Ruta por " + criterio + ": ");
        for (int i = 0; i < rutaFinal.length; i++) {
            salida.appendText("S" + rutaFinal[i]);
            if (i < rutaFinal.length - 1) salida.appendText(" → ");
        }
        salida.appendText(
                String.format("\n   Total: %.1f %s\n",
                        resultado.getPesoTotal(),
                        usarTiempo ? "seg" : "Q")
        );

        calcularPosiciones(total);

        dibujarGrafoBase(null);

        animacion = new Timeline(
                new KeyFrame(
                        Duration.millis(velocidadMs),
                        e -> animarPaso(destino)
                )
        );
        animacion.setCycleCount(rutaFinal.length);
        animacion.setOnFinished(e -> {
            salida.appendText("Animación completada\n");
            dibujarGrafoBase(rutaFinal);
        });
        animacion.play();
        salida.appendText("▶ Animando...\n");
    }



    private void animarPaso(int destino) {
        if (pasoActual >= rutaFinal.length) return;

        int idActual = rutaFinal[pasoActual];
        int idxActual = buscarIndice(idActual);
        if (idxActual != -1) visitado[idxActual] = true;

        salida.appendText("   Paso " + (pasoActual + 1) + ": visitando sucursal " + idActual + "\n");

        redibujarConEstado(pasoActual, destino);
        pasoActual++;
    }


    private void dibujarGrafoBase(int[] rutaResaltar) {
        Grafo  grafo = state.getGrafo();
        int    total = grafo.getTotalNodos();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);

        if (total == 0) {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font(14));
            gc.fillText("Carga sucursales y conexiones primero",
                    200, 200);
            return;
        }

        calcularPosiciones(total);

        dibujarAristas(gc, grafo, total, rutaResaltar);

        for (int i = 0; i < total; i++) {
            int id = grafo.getIdSucursal(i);
            boolean enRuta = enRuta(id, rutaResaltar);
            dibujarNodo(gc, i, id, enRuta
                    ? Color.web("#e74c3c")
                    : Color.web("#2980b9"));
        }
    }


    private void redibujarConEstado(int pasoActual, int destino) {
        Grafo  grafo = state.getGrafo();
        int    total = grafo.getTotalNodos();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);

        int[] subRuta = new int[pasoActual + 1];
        System.arraycopy(rutaFinal, 0, subRuta, 0, pasoActual + 1);
        dibujarAristas(gc, grafo, total, subRuta);

        for (int i = 0; i < total; i++) {
            int id = grafo.getIdSucursal(i);

            Color color;
            if (id == rutaFinal[pasoActual]) {
                color = Color.web("#f39c12");
            } else if (id == destino &&
                    pasoActual == rutaFinal.length - 1) {
                color = Color.web("#27ae60");
            } else if (visitado[i]) {
                color = Color.web("#e74c3c");
            } else {
                color = Color.web("#2980b9");
            }
            dibujarNodo(gc, i, id, color);
        }
    }


    private void dibujarAristas(GraphicsContext gc, Grafo grafo,
                                int total, int[] rutaResaltar) {
        for (int i = 0; i < total; i++) {
            int idOrigen = grafo.getIdSucursal(i);
            NodoArista arista = grafo.getAristas(i);

            while (arista != null) {
                int j = buscarIndice(arista.getDestino());
                if (j != -1 && j > i) {
                    boolean enRuta = enAristasRuta(
                            idOrigen, arista.getDestino(), rutaResaltar
                    );

                    gc.setStroke(enRuta
                            ? Color.web("#e74c3c")
                            : Color.web("#bdc3c7"));
                    gc.setLineWidth(enRuta ? 3 : 1.5);
                    gc.strokeLine(
                            posX[i], posY[i], posX[j], posY[j]
                    );

                    double mx = (posX[i] + posX[j]) / 2;
                    double my = (posY[i] + posY[j]) / 2;
                    gc.setFill(enRuta
                            ? Color.web("#c0392b")
                            : Color.web("#7f8c8d"));
                    gc.setFont(Font.font(9));
                    gc.fillText(
                            String.format("T:%.0f C:%.0f",
                                    arista.getTiempo(), arista.getCosto()),
                            mx + 2, my - 2
                    );
                }
                arista = arista.getSiguiente();
            }
        }
    }


    private void dibujarNodo(GraphicsContext gc, int idx,
                             int idSucursal, Color color) {
        double x = posX[idx];
        double y = posY[idx];

        gc.setFill(Color.web("#00000022"));
        gc.fillOval(x - RADIO + 2, y - RADIO + 2,
                RADIO * 2, RADIO * 2);

        gc.setFill(color);
        gc.fillOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 13));
        String idStr = String.valueOf(idSucursal);
        gc.fillText(idStr, x - idStr.length() * 4, y + 5);

        Sucursal s = state.getCargaCSV().buscarSucursal(idSucursal);
        if (s != null) {
            gc.setFill(Color.web("#2c3e50"));
            gc.setFont(Font.font(10));
            String nom = s.getNameSucursal();
            if (nom.length() > 14) nom = nom.substring(0, 13) + "..";
            gc.fillText(nom, x - 36, y + RADIO + 14);
        }
    }

    private void calcularPosiciones(int total) {
        posX = new double[total];
        posY = new double[total];
        double cx = CANVAS_W / 2.0;
        double cy = CANVAS_H / 2.0;
        double r  = Math.min(cx, cy) - 70;

        for (int i = 0; i < total; i++) {
            double angulo = 2 * Math.PI * i / total - Math.PI / 2;
            posX[i] = cx + r * Math.cos(angulo);
            posY[i] = cy + r * Math.sin(angulo);
        }
    }

    private int buscarIndice(int idSucursal) {
        Grafo grafo = state.getGrafo();
        for (int i = 0; i < grafo.getTotalNodos(); i++) {
            if (grafo.getIdSucursal(i) == idSucursal) return i;
        }
        return -1;
    }

    private boolean enRuta(int id, int[] ruta) {
        if (ruta == null) return false;
        for (int n : ruta) if (n == id) return true;
        return false;
    }

    private boolean enAristasRuta(int a, int b, int[] ruta) {
        if (ruta == null) return false;
        for (int i = 0; i < ruta.length - 1; i++) {
            if ((ruta[i] == a && ruta[i+1] == b) ||
                    (ruta[i] == b && ruta[i+1] == a))
                return true;
        }
        return false;
    }

    private HBox cuadroLeyenda(String hex, String texto) {
        Canvas c = new Canvas(14, 14);
        c.getGraphicsContext2D().setFill(Color.web(hex));
        c.getGraphicsContext2D().fillOval(0, 0, 14, 14);
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 11px;");
        HBox h = new HBox(5, c, l);
        return h;
    }
}