package clases;

import estructuras.arbolB.ArbolB;
import estructuras.arbolBPlus.ArbolBPlus;
import estructuras.avl.ArbolAVL;
import estructuras.hash.TablaHash;
import estructuras.lista.ListaEnlazada;
import estructuras.nodo.Cola;
import estructuras.nodo.Pila;
import Enum.Estado;

public class Sucursal {

    private int    idSucursal;
    private String nameSucursal;
    private String location;
    private int    entryTime;
    private int    transferTime;
    private int    dispatchInterval;

    private ListaEnlazada<Productos>  lista;
    private ArbolAVL<Productos>       avlNombre;
    private ArbolB<String>            arbolBFechas;
    private ArbolBPlus<String>        arbolBPlusCategoria;
    private TablaHash                 tablaHash;

    private Cola<Productos> colaIngreso;
    private Cola<Productos> colaTraspaso;
    private Cola<Productos> colaSalida;

    private Pila<OperacionProducto> pilaRollback;

    public Sucursal(int idSucursal, String nameSucursal, String location,
                    int entryTime, int transferTime, int dispatchInterval) {
        this.idSucursal       = idSucursal;
        this.nameSucursal     = nameSucursal;
        this.location         = location;
        this.entryTime        = entryTime;
        this.transferTime     = transferTime;
        this.dispatchInterval = dispatchInterval;

        this.lista                = new ListaEnlazada<>();
        this.avlNombre            = new ArbolAVL<>();
        this.arbolBFechas         = new ArbolB<>(3);
        this.arbolBPlusCategoria  = new ArbolBPlus<>(4);
        this.tablaHash            = new TablaHash();
        this.colaIngreso          = new Cola<>();
        this.colaTraspaso         = new Cola<>();
        this.colaSalida           = new Cola<>();
        this.pilaRollback         = new Pila<>();
    }


    public boolean agregarProducto(Productos p) {
        if (tablaHash.contiene(p.getBarCode())) {
            System.err.println("Duplicado: " + p.getBarCode());
            return false;
        }
        try {
            lista.agregar(p);
            avlNombre.insert(p);
            tablaHash.insertar(p.getBarCode(), p);
            arbolBFechas.insertar(p.getExpiryDate());
            arbolBPlusCategoria.insertar(p.getCategory());

            pilaRollback.push(new OperacionProducto("AGREGAR", p));
            return true;

        } catch (Exception e) {
            // Rollback parcial
            lista.remove(p);
            avlNombre.delete(p);
            tablaHash.eliminar(p.getBarCode());
            System.err.println("Rollback: " + p.getName());
            return false;
        }
    }



    public boolean eliminarProducto(String codigoBarras) {
        Productos p = tablaHash.buscar(codigoBarras);
        if (p == null) return false;

        lista.remove(p);
        avlNombre.delete(p);                   
        tablaHash.eliminar(codigoBarras);
        arbolBFechas.eliminar(p.getExpiryDate());
        arbolBPlusCategoria.eliminar(p.getCategory());

        pilaRollback.push(new OperacionProducto("ELIMINAR", p));
        return true;
    }



    public Productos buscarPorNombre(String nombre) {
        // Crear producto temporal solo para comparar
        Productos temp = new Productos(
                idSucursal, nombre, "", "", "", "", 0.0, 0
        );
        return avlNombre.search(temp);
    }

    public Productos buscarPorCodigo(String codigo) {
        return tablaHash.buscar(codigo);
    }

    public ListaEnlazada<String> buscarPorRangoFecha(
            String desde, String hasta) {
        return arbolBFechas.buscarRango(desde, hasta);
    }

    public ListaEnlazada<String> buscarPorCategoria(String categoria) {
        return arbolBPlusCategoria.buscarPorCategoria(categoria);
    }

    public ListaEnlazada<Productos> listarOrdenados() {
        return avlNombre.inOrden();
    }

    public OperacionProducto deshacerUltimaOperacion() {
        if (pilaRollback.isEmpty()) return null;
        OperacionProducto op = pilaRollback.pop();

        if (op.getTipo().equals("AGREGAR")) {
            lista.remove(op.getProducto());
            avlNombre.delete(op.getProducto());
            tablaHash.eliminar(op.getProducto().getBarCode());
        } else if (op.getTipo().equals("ELIMINAR")) {
            agregarProducto(op.getProducto());
        }
        return op;
    }

    public void recibirProducto(Productos p) {
        p.setStatus(Estado.TRANSITO);
        colaIngreso.enqueue(p);
    }

    public void prepararTraspaso(Productos p) {
        colaTraspaso.enqueue(p);
    }

    public void alistarSalida(Productos p) {
        colaSalida.enqueue(p);
    }

    public Productos despachar() {
        if (colaSalida.isEmpty()) return null;
        return colaSalida.dequeue();
    }

    public int    getIdSucursal()       { return idSucursal; }
    public String getNameSucursal()     { return nameSucursal; }
    public String getLocation()         { return location; }
    public int    getEntryTime()        { return entryTime; }
    public int    getTransferTime()     { return transferTime; }
    public int    getDispatchInterval() { return dispatchInterval; }

    public ListaEnlazada<Productos>  getLista()               { return lista; }
    public ArbolAVL<Productos>       getAvlNombre()           { return avlNombre; }
    public ArbolB<String>            getArbolBFechas()        { return arbolBFechas; }
    public ArbolBPlus<String>        getArbolBPlusCategoria() { return arbolBPlusCategoria; }
    public TablaHash                 getTablaHash()           { return tablaHash; }
    public Cola<Productos>           getColaIngreso()         { return colaIngreso; }
    public Cola<Productos>           getColaTraspaso()        { return colaTraspaso; }
    public Cola<Productos>           getColaSalida()          { return colaSalida; }

    @Override
    public String toString() {
        return idSucursal + " - " + nameSucursal + " (" + location + ")";
    }
}