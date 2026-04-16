package estructuras.lista;

import estructuras.nodo.Nodo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ListaEnlazada<T> {

    private Nodo<T> head;
    private int size;

    public ListaEnlazada() {
        head = null;
        size = 0;
    }

    public void agregar(T producto) {
        Nodo<T> newNodo = new Nodo<>(producto);
        if (head == null) {
            head = newNodo;
        } else {
            Nodo<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNodo;
        }
        size++;
    }

    public void mostrar() {
        if (head == null) {
            System.out.println("La lista está vacía");
            return;
        }

        Nodo<T> current = head;
        int i = 1;
        while (current != null) {
            System.out.println(i + ". " + current.getProducto());
            current = current.next;
            i++;
        }
    }
    public boolean remove(T producto) {
        if (head == null) {
            return false;
        }

        if (head.producto.equals(producto)) {
            head = head.next;
            size--;
            return true;
        }
        Nodo<T> current = head;
        while (current.next != null) {

            if (current.next.producto.equals(producto)) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public T buscar(Predicate<T> condicion) {
        Nodo<T> current = head;

        while (current != null) {

            if (condicion.test(current.producto)) {
                return current.producto;
            }

            current = current.next;
        }

        return null;
    }

    public boolean existe(T producto) {
        Nodo<T> current = head;

        while (current != null) {

            if (current.producto.equals(producto)) {
                return true;
            }

            current = current.next;
        }

        return false;
    }

    public T getIndice(int indice) {

        if (indice < 0 || indice >= size)
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        Nodo<T> current = head;
        for (int i = 0; i < indice; i++) {
            current = current.next;
        }
        return current.producto;
    }


    public List<T> toList() {
        List<T> result = new ArrayList<>();
        Nodo<T> current = head;

        while (current != null) {

            result.add(current.producto);
            current = current.next;
        }
        return result;
    }

    public void clear() {
        head = null;
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public Nodo<T> getHead() {
        return head;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Lista: ");
        Nodo<T> current = head;
        while (current != null) {
            sb.append(current.producto);
            if (current.next != null) sb.append(" → ");
            current = current.next;
        }
        return sb.toString();
    }
}