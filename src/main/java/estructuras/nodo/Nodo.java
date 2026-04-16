package estructuras.nodo;

public class Nodo<T> {

    public T producto;
    public Nodo<T> next;

    public Nodo(T producto) {
        this.producto = producto;
        this.next = null;
    }

    public T getProducto() {
        return producto;
    }

    public void setProducto(T producto) {
        this.producto = producto;
    }

    public Nodo<T> getNext() {
        return next;
    }

    public void setNext(Nodo<T> next) {
        this.next = next;
    }
}
