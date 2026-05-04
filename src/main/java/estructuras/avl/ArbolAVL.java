package estructuras.avl;

import estructuras.lista.ListaEnlazada;

public class ArbolAVL<T extends Comparable<T>> {

    private NodoAVL<T> raiz;

    public ArbolAVL() {
        raiz = null;
    }

    private int altura(NodoAVL<T> nodo) {
        return (nodo == null) ? 0 : nodo.altura;
    }

    private int factorBalanceo(NodoAVL<T> nodo) {
        return (nodo == null) ? 0
                : altura(nodo.left) - altura(nodo.right);
    }

    private void updateAltura(NodoAVL<T> nodo) {
        nodo.altura = 1 + Math.max(
                altura(nodo.left),
                altura(nodo.right)
        );
    }


    private NodoAVL<T> rotacionDerecha(NodoAVL<T> y) {
        NodoAVL<T> x = y.left;
        NodoAVL<T> temp = x.right;

        x.right = y;
        y.left = temp;

        updateAltura(y);
        updateAltura(x);
        return x;
    }

    private NodoAVL<T> rotacionIzquierda(NodoAVL<T> x) {
        NodoAVL<T> y = x.right;
        NodoAVL<T> temp = y.left;

        y.left = x;
        x.right = temp;

        updateAltura(x);
        updateAltura(y);
        return y;
    }

    private NodoAVL<T> balanceo(NodoAVL<T> nodo) {
        updateAltura(nodo);
        int fb = factorBalanceo(nodo);

        if (fb > 1 && factorBalanceo(nodo.left) >= 0)
            return rotacionDerecha(nodo);

        if (fb > 1 && factorBalanceo(nodo.left) < 0) {
            nodo.left = rotacionIzquierda(nodo.left);
            return rotacionDerecha(nodo);
        }

        if (fb < -1 && factorBalanceo(nodo.right) <= 0)
            return rotacionIzquierda(nodo);

        if (fb < -1 && factorBalanceo(nodo.right) > 0) {
            nodo.right = rotacionDerecha(nodo.right);
            return rotacionIzquierda(nodo);
        }

        return nodo;
    }


    public void insert(T dato) {
        raiz = insert(raiz, dato);
    }

    private NodoAVL<T> insert(NodoAVL<T> nodo, T dato) {
        if (nodo == null) return new NodoAVL<>(dato);
        int cmp = dato.compareTo(nodo.producto);

        if (cmp < 0) nodo.left = insert(nodo.left, dato);
        else if (cmp > 0) nodo.right = insert(nodo.right, dato);
        else return nodo;

        return balanceo(nodo);
    }


    public T search(T dato) {
        NodoAVL<T> resultado = search(raiz, dato);
        return (resultado != null) ? resultado.producto : null;
    }

    private NodoAVL<T> search(NodoAVL<T> nodo, T dato) {
        if (nodo == null) return null;

        int cmp = dato.compareTo(nodo.producto);

        if (cmp == 0) return nodo;
        if (cmp < 0) return search(nodo.left, dato);
        return search(nodo.right, dato);
    }


    public void delete(T dato) {
        raiz = delete(raiz, dato);
    }

    private NodoAVL<T> minNodo(NodoAVL<T> nodo) {
        while (nodo.left != null)
            nodo = nodo.left;
        return nodo;
    }

    private NodoAVL<T> delete(NodoAVL<T> nodo, T dato) {
        if (nodo == null) return null;

        int cmp = dato.compareTo(nodo.producto);

        if (cmp < 0) {
            nodo.left = delete(nodo.left, dato);
        } else if (cmp > 0) {
            nodo.right = delete(nodo.right, dato);
        } else {
            if (nodo.left == null) return nodo.right;

            if (nodo.right == null) return nodo.left;

            NodoAVL<T> sucesor = minNodo(nodo.right);
            nodo.producto = sucesor.producto;

            nodo.right = delete(nodo.right, sucesor.producto);
        }

        return balanceo(nodo);
    }


    public ListaEnlazada<T> inOrden() {
        ListaEnlazada<T> lista = new ListaEnlazada<>();
        inOrder(raiz,lista);
        return lista;
    }

    private void inOrder(NodoAVL<T> nodo, ListaEnlazada<T> lista) {
        if (nodo == null) return;
        inOrder(nodo.left, lista);
        lista.agregar(nodo.producto);
        inOrder(nodo.right, lista);
    }

    public NodoAVL<T> getRoot() {
        return raiz;
    }

    public int getHeight() {
        return altura(raiz);
    }

    public boolean isEmpty() {
        return raiz == null;
    }
}