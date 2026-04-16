package estructuras.arbolB;

import java.util.ArrayList;
import java.util.List;

public class NodoB <T extends Comparable<T>> {

    private List<T> claves;
    private List<NodoB<T>> hijos;
    private boolean esHoja;

    public NodoB(boolean esHoja) {
        this.esHoja = esHoja;
        this.claves = new ArrayList<>();
        this.hijos = new ArrayList<>();
    }

    public List<T> getClaves() {
        return claves;
    }

    public void setClaves(List<T> claves) {
        this.claves = claves;
    }

    public List<NodoB<T>> getHijos() {
        return hijos;
    }

    public void setHijos(List<NodoB<T>> hijos) {
        this.hijos = hijos;
    }

    public boolean isEsHoja() {
        return esHoja;
    }

    public void setEsHoja(boolean esHoja) {
        this.esHoja = esHoja;
    }
}
