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

    public T getProducto() {
        return producto;
    }

    public void setProducto(T producto) {
        this.producto = producto;
    }

    public NodoAVL<T> getLeft() {
        return left;
    }

    public void setLeft(NodoAVL<T> left) {
        this.left = left;
    }

    public NodoAVL<T> getRight() {
        return right;
    }

    public void setRight(NodoAVL<T> right) {
        this.right = right;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }
}
