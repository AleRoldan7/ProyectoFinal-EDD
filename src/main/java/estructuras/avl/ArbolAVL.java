package estructuras.avl;

import estructuras.nodo.Nodo;

import java.util.ArrayList;
import java.util.List;

public class ArbolAVL<T extends Comparable<T>> {

    private NodoAVL raiz;

    public ArbolAVL() {
        raiz = null;
    }

    private int altura(NodoAVL nodoAVL) {
        return (nodoAVL == null) ? 0 : nodoAVL.altura;
    }

    private int factorBalanceo(NodoAVL nodoAVL) {
        return (nodoAVL == null) ? 0 : altura(nodoAVL.left) - altura(nodoAVL.right);
    }

    private void updateAltura(NodoAVL nodoAVL) {
        nodoAVL.altura = 1 + Math.max(altura(nodoAVL.left), altura(nodoAVL.right));
    }

    private NodoAVL rotacionDerecha(NodoAVL y) {

        NodoAVL x = y.left;
        NodoAVL nodo = x.right;

        x.right = y;
        y.left = nodo;

        updateAltura(y);
        updateAltura(x);
        return x;
    }

    private NodoAVL rotacionIzquierda(NodoAVL x) {

        NodoAVL y = x.right;
        NodoAVL nodo = y.left;

        y.left = x;
        x.right = nodo;

        updateAltura(x);
        updateAltura(y);
        return y;
    }

    private NodoAVL balanceo(NodoAVL nodo) {
        updateAltura(nodo);
        int bf = factorBalanceo(nodo);

        // Caso Izquierda-Izquierda
        if (bf > 1 && factorBalanceo(nodo.left) >= 0)
            return rotacionDerecha(nodo);

        // Caso Izquierda-Derecha
        if (bf > 1 && factorBalanceo(nodo.left) < 0) {
            nodo.left = rotacionIzquierda(nodo.left); // primero rota el hijo
            return rotacionDerecha(nodo);       // luego rota la raíz
        }

        // Caso Derecha-Derecha
        if (bf < -1 && factorBalanceo(nodo.right) <= 0)
            return rotacionIzquierda(nodo);

        // Caso Derecha-Izquierda
        if (bf < -1 && factorBalanceo(nodo.right) > 0) {
            nodo.right = rotacionDerecha(nodo.right); // primero rota el hijo
            return rotacionIzquierda(nodo);           // luego rota la raíz
        }

        return nodo;
    }


    public void insert(T producto) {
        raiz = insert(raiz, producto);
    }

    private NodoAVL insert(NodoAVL node, T producto) {
        if (node == null) return new NodoAVL(producto);

        int cmp = producto.compareTo(producto);

        if (cmp < 0) {
            node.left = insert(node.left, producto);
        } else if (cmp > 0) {
            node.right = insert(node.right, producto);
        } else {
            return node;
        }

        return balanceo(node);
    }


    public T search(T producto) {
        NodoAVL result = search(raiz, producto);
        return (result != null) ? producto : null;
    }

    private NodoAVL search(NodoAVL node, T producto) {
        // No encontrado
        if (node == null) return null;

        int cmp = producto.compareTo(producto);

        if (cmp == 0) return node;        // encontrado
        if (cmp < 0) return search(node.left, producto);  // buscar izquierda
        return search(node.right, producto);               // buscar derecha
    }


    public void delete(T producto) {
        raiz = delete(raiz, producto);
    }

    // Encuentra el nodo con el valor mínimo de un subárbol
    // (se usa para reemplazar el nodo eliminado)
    private NodoAVL minNode(NodoAVL node) {
        while (node.left != null)
            node = node.left;
        return node;
    }

    private NodoAVL delete(NodoAVL node, T producto) {
        if (node == null) return null;

        int cmp = producto.compareTo(producto);

        if (cmp < 0) {
            node.left = delete(node.left, producto);
        } else if (cmp > 0) {
            node.right = delete(node.right, producto);
        } else {
            // Encontramos el nodo a eliminar — 3 casos:

            // Caso 1: no tiene hijo izquierdo
            if (node.left == null) return node.right;

            // Caso 2: no tiene hijo derecho
            if (node.right == null) return node.left;

            // Caso 3: tiene dos hijos
            // Reemplazamos con el sucesor inOrder
            // (el menor del subárbol derecho)
            NodoAVL successor = minNode(node.right);
            node.producto = successor.producto;
            node.right = delete(node.right, producto);
        }

        // Balancear al regresar
        return balanceo(node);
    }


    // InOrder → retorna elementos ordenados alfabéticamente
    public List<T> inOrder() {
        List<T> result = new ArrayList<>();
        inOrder(raiz, result);
        return result;
    }

    private void inOrder(NodoAVL node, List<T> result) {
        if (node == null) return;
        inOrder(node.left, result);   // primero izquierda
        result.add((T) node.producto);        // luego este nodo
        inOrder(node.right, result);  // luego derecha
    }


    public boolean isEmpty() {
        return raiz == null;
    }

    public NodoAVL getRoot() {
        return raiz;
    }

    // Altura total del árbol (para mostrar en UI)
    public int getHeight() {
        return altura(raiz);
    }

}
