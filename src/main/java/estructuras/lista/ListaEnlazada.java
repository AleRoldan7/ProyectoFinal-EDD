package estructuras.lista;

import estructuras.nodo.Nodo;

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

}