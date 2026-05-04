package estructuras.nodo;

public class Pila<T> {

    private Nodo<T> tope;
    private int size;

    public Pila() {
        tope = null;
        size = 0;
    }

    public void push(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.next    = tope;
        tope          = nuevo;
        size++;
    }

    public T pop() {
        if (isEmpty())
            throw new RuntimeException("Pila vacía");
        T dato = tope.producto;
        tope   = tope.next;
        size--;
        return dato;
    }

    public T peek() {
        if (isEmpty())
            throw new RuntimeException("Pila vacía");
        return tope.producto;
    }

    public boolean isEmpty() { return tope == null; }
    public int size()        { return size; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Pila (tope -> fondo): ");
        Nodo<T> actual = tope;
        while (actual != null) {
            sb.append(actual.producto);
            if (actual.next != null) sb.append(" -> ");
            actual = actual.next;
        }
        return sb.toString();
    }
}

