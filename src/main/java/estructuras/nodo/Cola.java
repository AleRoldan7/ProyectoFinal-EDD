package estructuras.nodo;

public class Cola<T> {

    private Nodo<T> frente; // primer elemento (sale primero)
    private Nodo<T> fin;    // último elemento (entra último)
    private int size;

    public Cola() {
        frente = null;
        fin    = null;
        size   = 0;
    }

    // Agregar al final — O(1)
    public void enqueue(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (fin == null) {
            frente = fin = nuevo;
        } else {
            fin.next = nuevo;
            fin      = nuevo;
        }
        size++;
    }

    // Sacar del frente — O(1)
    public T dequeue() {
        if (isEmpty())
            throw new RuntimeException("Cola vacía");
        T dato = frente.producto;
        frente = frente.next;
        if (frente == null) fin = null;
        size--;
        return dato;
    }

    // Ver el frente sin sacar — O(1)
    public T peek() {
        if (isEmpty())
            throw new RuntimeException("Cola vacía");
        return frente.producto;
    }

    // Para visualizar en la UI
    public java.util.List<T> toList() {
        java.util.List<T> lista = new java.util.ArrayList<>();
        Nodo<T> actual = frente;
        while (actual != null) {
            lista.add(actual.producto);
            actual = actual.next;
        }
        return lista;
    }

    public boolean isEmpty() { return frente == null; }
    public int size()        { return size; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cola (frente→fin): ");
        Nodo<T> actual = frente;
        while (actual != null) {
            sb.append(actual.producto);
            if (actual.next != null) sb.append(" → ");
            actual = actual.next;
        }
        return sb.toString();
    }
}