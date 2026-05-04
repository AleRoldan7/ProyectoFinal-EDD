package estructuras.nodo;

public class Cola<T> {

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int size;

    public Cola() {
        frente = null;
        fin    = null;
        size   = 0;
    }

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

    public T dequeue() {
        if (isEmpty())
            throw new RuntimeException("Cola vacía");
        T dato = frente.producto;
        frente = frente.next;
        if (frente == null) fin = null;
        size--;
        return dato;
    }

    public T peek() {
        if (isEmpty())
            throw new RuntimeException("Cola vacía");
        return frente.producto;
    }


    public boolean isEmpty() { return frente == null; }
    public int size()        { return size; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cola (frente -> fin): ");
        Nodo<T> actual = frente;
        while (actual != null) {
            sb.append(actual.producto);
            if (actual.next != null) sb.append(" -> ");
            actual = actual.next;
        }
        return sb.toString();
    }
}