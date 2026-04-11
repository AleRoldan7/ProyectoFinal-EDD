package estructuras.nodo;

public class Nodo<T> {

    public T producto;
    public Nodo<T> next;

    public Nodo(T producto) {
        this.producto = producto;
        this.next = null;
    }
}
