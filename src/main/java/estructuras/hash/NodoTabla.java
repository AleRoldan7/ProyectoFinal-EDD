package estructuras.hash;

import clases.Productos;

public class NodoTabla {

    private String clave;
    private Productos productos;
    NodoTabla siguiente;

    public NodoTabla(String clave, Productos productos) {
        this.clave = clave;
        this.productos = productos;
        this.siguiente = null;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Productos getProductos() {
        return productos;
    }

    public void setProductos(Productos productos) {
        this.productos = productos;
    }

    public NodoTabla getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoTabla siguiente) {
        this.siguiente = siguiente;
    }
}
