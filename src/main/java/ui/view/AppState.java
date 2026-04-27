package ui.view;

import clases.Productos;
import estructuras.avl.ArbolAVL;
import estructuras.grafos.Grafo;
import utils.CargaCSV;

public class AppState {

    private static AppState instancia;

    private CargaCSV cargaCSV;
    private Grafo grafo;
    private ArbolAVL<Productos> avlGlobal;

    private AppState() {
        cargaCSV = new CargaCSV();
        grafo = new Grafo();
        avlGlobal = new ArbolAVL<>();
    }

    public static AppState getInstance() {
        if (instancia == null)
            instancia = new AppState();
        return instancia;
    }

    public CargaCSV getCargaCSV() {
        return cargaCSV;
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public ArbolAVL<Productos> getAvlGlobal() {
        return avlGlobal;
    }
}
