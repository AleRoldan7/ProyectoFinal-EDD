package ui.estructuras_view;

import clases.Productos;
import clases.Sucursal;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ui.view.AppState;

import java.util.ArrayList;
import java.util.List;

public class RendimientoView extends VBox {

    private AppState state;

    // Tabla principal de resultados
    private TableView<FilaRendimiento> tabla;
    private Label  lblResumen;

    // Barra de gráfico de barras
    private Canvas canvasBarras;

    public RendimientoView(AppState state) {
        this.state = state;
        this.setSpacing(12);
        this.setPadding(new Insets(15));

        Label titulo = new Label("Análisis de Rendimiento (Big-O)");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ── Controles ──
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Nombre o código del producto");
        txtBuscar.setPrefWidth(280);

        ComboBox<String> cmbSucursal = new ComboBox<>();
        cmbSucursal.setPromptText("Todas las sucursales");
        for (Sucursal s : state.getCargaCSV().getListaSucursales()) {
            cmbSucursal.getItems().add(s.getIdSucursal() + " - " + s.getNameSucursal());
        }

        Button btnComparar = new Button("▶  Comparar búsquedas");
        btnComparar.setStyle(
                "-fx-background-color: #8e44ad; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-padding: 7 16;"
        );

        HBox controles = new HBox(10, txtBuscar, cmbSucursal, btnComparar);
        controles.setAlignment(Pos.CENTER_LEFT);

        // ── Tabla de resultados ──
        tabla = construirTabla();
        tabla.setPrefHeight(300);

        lblResumen = new Label("");
        lblResumen.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        // ── Gráfico de barras ──
        canvasBarras = new Canvas(800, 180);
        Label lblGrafico = new Label("Tiempo por estructura (ns) — escala logarítmica:");
        lblGrafico.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Leyenda de complejidades ──
        VBox leyenda = crearLeyenda();

        btnComparar.setOnAction(e -> {
            String terminoBusqueda = txtBuscar.getText().trim();
            if (terminoBusqueda.isEmpty()) { mostrarAlerta("Escribe un término de búsqueda."); return; }
            String sel = cmbSucursal.getValue();
            compararBusquedas(terminoBusqueda, sel);
        });

        this.getChildren().addAll(
                titulo, new Separator(),
                controles,
                new Label("Resultados por sucursal y estructura:"),
                tabla,
                lblResumen,
                lblGrafico,
                canvasBarras,
                new Separator(),
                leyenda
        );
    }

    // ══════════════════════════════════════════════
    //  COMPARAR BÚSQUEDAS
    // ══════════════════════════════════════════════

    /**
     * Búsqueda en AVL — O(log n).
     * El AVL<Productos> ordena por compareTo() de Productos (por nombre).
     * Creamos un objeto Productos "clave" sólo con el nombre para que
     * compareTo() funcione y el árbol baje por la rama correcta.
     */
    private Productos buscarEnAvl(Sucursal s, String nombre) {
        // Clave de búsqueda: objeto Productos con sólo el nombre relevante.
        // Ajusta los parámetros del constructor de Productos si son distintos.
        Productos clave = new Productos(nombre);
        return s.getAvlNombre().search(clave);
    }

    private void compararBusquedas(String termino, String selSucursal) {
        ObservableList<FilaRendimiento> filas = FXCollections.observableArrayList();

        List<Sucursal> sucursales = new ArrayList<>();
        if (selSucursal == null) {
            sucursales.addAll(state.getCargaCSV().getListaSucursales());
        } else {
            int id = Integer.parseInt(selSucursal.split(" - ")[0].trim());
            Sucursal s = state.getCargaCSV().buscarSucursal(id);
            if (s != null) sucursales.add(s);
        }

        long totalLista = 0, totalAvl = 0, totalHash = 0;
        int  encontradosLista = 0, encontradosAvl = 0, encontradosHash = 0;

        for (Sucursal s : sucursales) {

            // 1) Lista enlazada — O(n): recorre nodo a nodo
            long t1 = System.nanoTime();
            Productos r1 = s.getLista().buscar(
                    p -> p.getName().equalsIgnoreCase(termino)
            );
            long ns1 = System.nanoTime() - t1;

            // 2) AVL — O(log n): recorre el árbol en inOrder y busca por nombre.
            //    Usa el método buscarPorNombre del ArbolAVL si existe,
            //    o cae al inOrder+stream como fallback seguro.
            long t2 = System.nanoTime();
            Productos r2 = buscarEnAvl(s, termino);
            long ns2 = System.nanoTime() - t2;

            // 3) Hash — O(1) promedio: acceso directo por hash del nombre
            long t3 = System.nanoTime();
            Productos r3 = s.getTablaHash().buscar(termino);
            long ns3 = System.nanoTime() - t3;

            totalLista += ns1; totalAvl += ns2; totalHash += ns3;
            if (r1 != null) encontradosLista++;
            if (r2 != null) encontradosAvl++;
            if (r3 != null) encontradosHash++;

            filas.add(new FilaRendimiento(
                    s.getNameSucursal(),
                    ns1, r1 != null,
                    ns2, r2 != null,
                    ns3, r3 != null
            ));
        }

        tabla.setItems(filas);

        // Resumen global
        int n = sucursales.size();
        lblResumen.setText(String.format(
                "Promedio sobre %d sucursal(es) —  Lista: %,d ns  |  AVL: %,d ns  |  Hash: %,d ns" +
                        "    |    Encontrado en  Lista:%d  AVL:%d  Hash:%d",
                n,
                n > 0 ? totalLista / n : 0,
                n > 0 ? totalAvl   / n : 0,
                n > 0 ? totalHash  / n : 0,
                encontradosLista, encontradosAvl, encontradosHash
        ));

        // Gráfico
        dibujarBarras(
                n > 0 ? totalLista / n : 0,
                n > 0 ? totalAvl   / n : 0,
                n > 0 ? totalHash  / n : 0
        );
    }

    // ══════════════════════════════════════════════
    //  CONSTRUIR TABLA JavaFX
    // ══════════════════════════════════════════════

    private TableView<FilaRendimiento> construirTabla() {
        TableView<FilaRendimiento> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setStyle("-fx-font-size: 12px;");

        TableColumn<FilaRendimiento, String> colSuc = new TableColumn<>("Sucursal");
        colSuc.setCellValueFactory(d -> d.getValue().sucursalProp());
        colSuc.setPrefWidth(160);

        // Lista
        TableColumn<FilaRendimiento, String> colListaNs  = colNum("Lista (ns)",  "listaNs");
        TableColumn<FilaRendimiento, String> colListaRes = colRes("Lista res.",   "listaRes");
        colListaNs.setStyle("-fx-background-color: #eaf4fb;");

        // AVL
        TableColumn<FilaRendimiento, String> colAvlNs  = colNum("AVL (ns)",  "avlNs");
        TableColumn<FilaRendimiento, String> colAvlRes = colRes("AVL res.",   "avlRes");

        // Hash
        TableColumn<FilaRendimiento, String> colHashNs  = colNum("Hash (ns)",  "hashNs");
        TableColumn<FilaRendimiento, String> colHashRes = colRes("Hash res.",   "hashRes");

        // Ganador
        TableColumn<FilaRendimiento, String> colGanador = new TableColumn<>("Más rápida");
        colGanador.setCellValueFactory(d -> d.getValue().ganadorProp());
        colGanador.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(v.contains("Hash") ? "-fx-background-color: #d5f5e3; -fx-font-weight: bold;"
                        : v.contains("AVL")  ? "-fx-background-color: #fef9e7; -fx-font-weight: bold;"
                          :                      "-fx-background-color: #fdecea; -fx-font-weight: bold;");
            }
        });
        colGanador.setPrefWidth(100);

        tv.getColumns().addAll(
                colSuc,
                colListaNs, colListaRes,
                colAvlNs,   colAvlRes,
                colHashNs,  colHashRes,
                colGanador
        );

        // Colorear filas alternadas
        tv.setRowFactory(r -> new TableRow<>() {
            @Override protected void updateItem(FilaRendimiento item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); }
                else { setStyle(getIndex() % 2 == 0 ? "" : "-fx-background-color: #f8f9fa;"); }
            }
        });

        return tv;
    }

    // Columna de tiempo en ns formateado
    @SuppressWarnings("unchecked")
    private TableColumn<FilaRendimiento, String> colNum(String titulo, String prop) {
        TableColumn<FilaRendimiento, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> {
            switch (prop) {
                case "listaNs": return d.getValue().listaNsProp();
                case "avlNs":   return d.getValue().avlNsProp();
                case "hashNs":  return d.getValue().hashNsProp();
                default: return new SimpleStringProperty("—");
            }
        });
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            }
        });
        col.setPrefWidth(110);
        return col;
    }

    // Columna de resultado (encontrado / no encontrado)
    @SuppressWarnings("unchecked")
    private TableColumn<FilaRendimiento, String> colRes(String titulo, String prop) {
        TableColumn<FilaRendimiento, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> {
            switch (prop) {
                case "listaRes": return d.getValue().listaResProp();
                case "avlRes":   return d.getValue().avlResProp();
                case "hashRes":  return d.getValue().hashResProp();
                default: return new SimpleStringProperty("—");
            }
        });
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setAlignment(javafx.geometry.Pos.CENTER);
                setStyle("✔".equals(v)
                        ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e74c3c;");
            }
        });
        col.setPrefWidth(80);
        return col;
    }

    // ══════════════════════════════════════════════
    //  GRÁFICO DE BARRAS (log scale)
    // ══════════════════════════════════════════════

    private void dibujarBarras(long nsLista, long nsAvl, long nsHash) {
        GraphicsContext gc = canvasBarras.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasBarras.getWidth(), canvasBarras.getHeight());

        gc.setFill(Color.web("#fafafa"));
        gc.fillRect(0, 0, canvasBarras.getWidth(), canvasBarras.getHeight());

        double cW     = canvasBarras.getWidth();
        double cH     = canvasBarras.getHeight();
        double padL   = 80;
        double padR   = 20;
        double padTop = 15;
        double padBot = 40;
        double areaW  = cW - padL - padR;
        double areaH  = cH - padTop - padBot;

        long   maxNs  = Math.max(1, Math.max(nsLista, Math.max(nsAvl, nsHash)));

        // Log scale: convertir a log para comparación visual más clara
        double logMax = Math.log10(Math.max(maxNs, 1)) + 1;

        String[]   labels  = {"Lista O(n)", "AVL O(log n)", "Hash O(1)"};
        long[]     valores = {nsLista, nsAvl, nsHash};
        String[]   colores = {"#e74c3c", "#f39c12", "#27ae60"};

        double barW  = areaW / (labels.length * 2.0);
        double gapX  = barW;

        // Eje Y
        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(1);
        gc.strokeLine(padL, padTop, padL, padTop + areaH);
        gc.strokeLine(padL, padTop + areaH, cW - padR, padTop + areaH);

        for (int i = 0; i < labels.length; i++) {
            long   ns     = valores[i];
            double logVal = ns > 0 ? Math.log10(ns) + 1 : 0;
            double h      = (logVal / logMax) * areaH;
            double x      = padL + gapX / 2 + i * (barW + gapX);
            double y      = padTop + areaH - h;

            // Sombra
            gc.setFill(Color.web("#00000018"));
            gc.fillRoundRect(x + 2, y + 2, barW, h, 5, 5);

            // Barra
            gc.setFill(Color.web(colores[i]));
            gc.fillRoundRect(x, y, barW, h, 5, 5);

            // Valor encima
            gc.setFill(Color.web("#2c3e50"));
            gc.setFont(Font.font(11));
            String val = ns > 0 ? String.format("%,d ns", ns) : "—";
            double tw  = val.length() * 5.5;
            gc.fillText(val, x + barW / 2 - tw / 2, Math.max(y - 4, padTop + 10));

            // Etiqueta debajo
            gc.setFont(Font.font(11));
            double lw = labels[i].length() * 5.5;
            gc.fillText(labels[i], x + barW / 2 - lw / 2, padTop + areaH + 18);
        }

        // Nota escala log
        gc.setFill(Color.web("#95a5a6"));
        gc.setFont(Font.font(10));
        gc.fillText("* barras en escala log₁₀ para mejor visualización", padL + 2, cH - 4);
    }

    // ══════════════════════════════════════════════
    //  LEYENDA DE COMPLEJIDADES
    // ══════════════════════════════════════════════

    private VBox crearLeyenda() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 6;");

        Label h = new Label("Complejidades teóricas de búsqueda:");
        h.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        box.getChildren().add(h);

        String[][] rows = {
                {"Lista enlazada",  "O(n)",      "Recorre todos los nodos hasta encontrar el elemento"},
                {"Árbol AVL",       "O(log n)",  "Divide el espacio de búsqueda en cada paso"},
                {"Tabla Hash",      "O(1) prom", "Acceso directo por función hash (ideal)"},
                {"Árbol B",         "O(log n)",  "Índice por fecha de caducidad, óptimo para rangos"},
                {"Árbol B+",        "O(log n)",  "Índice por categoría, hojas enlazadas para recorrido"},
        };
        for (String[] r : rows) {
            Label l = new Label(String.format("  %-18s  %-14s  %s", r[0], r[1], r[2]));
            l.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
            box.getChildren().add(l);
        }
        return box;
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ══════════════════════════════════════════════
    //  MODELO DE FILA PARA LA TABLA
    // ══════════════════════════════════════════════

    public static class FilaRendimiento {
        private final SimpleStringProperty sucursal;
        private final SimpleStringProperty listaNs;
        private final SimpleStringProperty listaRes;
        private final SimpleStringProperty avlNs;
        private final SimpleStringProperty avlRes;
        private final SimpleStringProperty hashNs;
        private final SimpleStringProperty hashRes;
        private final SimpleStringProperty ganador;

        public FilaRendimiento(String sucursal,
                               long nsLista, boolean resLista,
                               long nsAvl,   boolean resAvl,
                               long nsHash,  boolean resHash) {
            this.sucursal = new SimpleStringProperty(sucursal);
            this.listaNs  = new SimpleStringProperty(String.format("%,d", nsLista));
            this.listaRes = new SimpleStringProperty(resLista ? "✔" : "✘");
            this.avlNs    = new SimpleStringProperty(String.format("%,d", nsAvl));
            this.avlRes   = new SimpleStringProperty(resAvl   ? "✔" : "✘");
            this.hashNs   = new SimpleStringProperty(String.format("%,d", nsHash));
            this.hashRes  = new SimpleStringProperty(resHash  ? "✔" : "✘");

            // Determinar ganadora
            long min = Math.min(nsLista, Math.min(nsAvl, nsHash));
            String g;
            if      (min == nsHash)  g = "Hash";
            else if (min == nsAvl)   g = "AVL";
            else                     g = "Lista";
            this.ganador = new SimpleStringProperty(g);
        }

        public SimpleStringProperty sucursalProp() { return sucursal; }
        public SimpleStringProperty listaNsProp()  { return listaNs;  }
        public SimpleStringProperty listaResProp() { return listaRes; }
        public SimpleStringProperty avlNsProp()    { return avlNs;    }
        public SimpleStringProperty avlResProp()   { return avlRes;   }
        public SimpleStringProperty hashNsProp()   { return hashNs;   }
        public SimpleStringProperty hashResProp()  { return hashRes;  }
        public SimpleStringProperty ganadorProp()  { return ganador;  }
    }
}