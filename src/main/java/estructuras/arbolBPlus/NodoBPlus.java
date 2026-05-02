package estructuras.arbolBPlus;

import java.util.ArrayList;
import java.util.List;

public class NodoBPlus<T extends Comparable<T>> {

    private T[] claves;
    private NodoBPlus<T>[] hijos;
    private NodoBPlus<T> siguiente;
    private boolean esHoja;
    private int numClaves;

    public NodoBPlus(boolean esHoja, int orden) {
        this.esHoja = esHoja;
        this.claves = (T[]) new Comparable[orden];
        this.hijos = new NodoBPlus[orden + 1];
        this.siguiente = null;
        this.numClaves = 0;
    }

    public T[] getClaves() {
        return claves;
    }

    public void setClaves(T[] claves) {
        this.claves = claves;
    }

    public NodoBPlus<T>[] getHijos() {
        return hijos;
    }

    public void setHijos(NodoBPlus<T>[] hijos) {
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

    public int getNumClaves() {
        return numClaves;
    }

    public void setNumClaves(int numClaves) {
        this.numClaves = numClaves;
    }
}
