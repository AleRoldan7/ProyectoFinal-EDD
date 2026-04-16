package estructuras.avl;

public class NodoAVL<T> {

    public T producto;
    NodoAVL left;
    NodoAVL right;

    int altura;

    public NodoAVL(T producto) {

        this.producto = producto;
        this.left = null;
        this.right = null;
        this.altura = 1;

    }


}
