package estructuras.avl;

public class NodoAVL<T> {

    public T       producto;
    public NodoAVL<T> left;
    public NodoAVL<T> right;
    public int     altura;

    public NodoAVL(T producto) {
        this.producto = producto;
        this.left     = null;
        this.right    = null;
        this.altura   = 1;
    }


}
