package estructuras.grafos;

public class NodoArista {

    private int destino;
    private double tiempo;
    private double costo;
    private NodoArista siguiente;

    public NodoArista(int destino, double tiempo, double costo) {
        this.destino = destino;
        this.tiempo = tiempo;
        this.costo = costo;
        this.siguiente = null;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    public double getTiempo() {
        return tiempo;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public NodoArista getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoArista siguiente) {
        this.siguiente = siguiente;
    }
}
