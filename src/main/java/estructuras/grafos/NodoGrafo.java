package estructuras.grafos;

public class NodoGrafo {

    private int idSucursal;
    private NodoArista listaArista;

    public NodoGrafo(int idSucursal) {
        this.idSucursal = idSucursal;
        this.listaArista = null;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public NodoArista getListaArista() {
        return listaArista;
    }

    public void setListaArista(NodoArista listaArista) {
        this.listaArista = listaArista;
    }
}
