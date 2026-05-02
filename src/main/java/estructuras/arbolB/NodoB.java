package estructuras.arbolB;

import estructuras.nodo.Nodo;

import java.util.ArrayList;
import java.util.List;

public class NodoB <T extends Comparable<T>> {


    private T[] claves;
    private NodoB<T>[] hijos;
    private int numClaves;
    private boolean esHoja;

    public NodoB(boolean esHoja, int orden) {
        this.esHoja = esHoja;
        this.claves = (T[]) new Comparable[2 * orden - 1];
        this.hijos = new NodoB[2 * orden];
        this.numClaves = 0;
    }

    public T[] getClaves() {
        return claves;
    }

    public void setClaves(T[] claves) {
        this.claves = claves;
    }

    public NodoB<T>[] getHijos() {
        return hijos;
    }

    public void setHijos(NodoB<T>[] hijos) {
        this.hijos = hijos;
    }

    public int getNumClaves() {
        return numClaves;
    }

    public void setNumClaves(int numClaves) {
        this.numClaves = numClaves;
    }

    public boolean isEsHoja() {
        return esHoja;
    }

    public void setEsHoja(boolean esHoja) {
        this.esHoja = esHoja;
    }

}
