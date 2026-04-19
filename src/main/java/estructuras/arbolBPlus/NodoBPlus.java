package estructuras.arbolBPlus;

import java.util.ArrayList;
import java.util.List;

public class NodoBPlus <T extends Comparable<T>> {

    private List<T> claves;
    private List<NodoBPlus<T>> hijos;
    NodoBPlus<T> siguiente;
    private boolean esHoja;

    public NodoBPlus(boolean esHoja) {
        this.claves = new ArrayList<>();
        this.hijos = new ArrayList<>();
        this.siguiente = null;
        this.esHoja = esHoja;
    }

    public List<T> getClaves() {
        return claves;
    }

    public void setClaves(List<T> claves) {
        this.claves = claves;
    }

    public List<NodoBPlus<T>> getHijos() {
        return hijos;
    }

    public void setHijos(List<NodoBPlus<T>> hijos) {
        this.hijos = hijos;
    }

    public NodoBPlus<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoBPlus<T> siguiente) {
        this.siguiente = siguiente;
    }

    public boolean isEsHoja() {
        return esHoja;
    }

    public void setEsHoja(boolean esHoja) {
        this.esHoja = esHoja;
    }
}
