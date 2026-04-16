package estructuras.arbolB;

public class ArbolB <T extends Comparable<T>> {

    private NodoB<T> raiz;
    private int orden;

    public ArbolB(int orden) {
        this.raiz = new NodoB<>(true);
        this.orden = orden;
    }

    public NodoB<T> getRaiz() {
        return raiz;
    }

    public void insertar(T producto) {
        NodoB<T> nodo = raiz;

        if (nodo.getClaves().size() == orden - 1) {
            NodoB<T> nuevaRaiz = new NodoB<>(false);
            nuevaRaiz.getHijos().add(nodo);


        }
    }
}
