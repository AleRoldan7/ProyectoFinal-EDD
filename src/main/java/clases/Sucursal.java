package clases;

import estructuras.arbolB.ArbolB;
import estructuras.arbolBPlus.ArbolBPlus;
import estructuras.hash.TablaHash;
import estructuras.lista.ListaEnlazada;
import estructuras.nodo.Cola;
import estructuras.nodo.Pila;
import Enum.Estado;

public class Sucursal {

    private int idSucursal;
    private String nameSucursal;
    private String location;
    private int entryTime;
    private int transferTime;
    private int dispatchInterval;

    // Cada sucursal tiene sus propias estructuras
    private ListaEnlazada<Productos> lista;
    private ArbolB<String> arbolBFechas;           // clave: fecha de expiración
    private ArbolBPlus<String> arbolBPlusCategoria; // clave: categoría
    private TablaHash tablaHash;                    // clave: código de barra

    // AVL lo manejaremos desde el CatalogoService global
    // porque el enunciado pide comparar búsquedas entre sucursales

    // Tres colas de despacho
    private Cola<Productos> colaIngreso;
    private Cola<Productos> colaTraspaso;
    private Cola<Productos> colaSalida;

    // Pila de rollback
    private Pila<OperacionProducto> pilaRollback;

    public Sucursal(int idSucursal, String nameSucursal, String location,
                    int entryTime, int transferTime, int dispatchInterval) {
        this.idSucursal = idSucursal;
        this.nameSucursal = nameSucursal;
        this.location = location;
        this.entryTime = entryTime;
        this.transferTime = transferTime;
        this.dispatchInterval = dispatchInterval;

        this.lista = new ListaEnlazada<>();
        this.arbolBFechas = new ArbolB<>(3);
        this.arbolBPlusCategoria = new ArbolBPlus<>(4);
        this.tablaHash = new TablaHash();
        this.colaIngreso = new Cola<>();
        this.colaTraspaso = new Cola<>();
        this.colaSalida = new Cola<>();
        this.pilaRollback = new Pila<>();
    }

    // ─────────────────────────────────────────
    // AGREGAR PRODUCTO (inserción atomizada)
    // ─────────────────────────────────────────

    public boolean agregarProducto(Productos p) {
        // Verificar duplicado por código de barra
        if (tablaHash.contiene(p.getBarCode())) {
            System.err.println("Duplicado: " + p.getBarCode());
            return false;
        }

        try {
            lista.agregar(p);
            tablaHash.insertar(p.getBarCode(), p);
            arbolBFechas.insertar(p.getExpiryDate());
            arbolBPlusCategoria.insertar(p.getCategory());

            // Guardar en rollback
            pilaRollback.push(new OperacionProducto("AGREGAR", p));
            return true;

        } catch (Exception e) {
            // Rollback parcial
            lista.remove(p);
            tablaHash.eliminar(p.getBarCode());
            System.err.println("Rollback ejecutado: " + p.getName());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // ELIMINAR PRODUCTO
    // ─────────────────────────────────────────

    public boolean eliminarProducto(String codigoBarras) {
        Productos p = tablaHash.buscar(codigoBarras);
        if (p == null) return false;

        lista.remove(p);
        tablaHash.eliminar(codigoBarras);
        arbolBFechas.eliminar(p.getExpiryDate());
        arbolBPlusCategoria.eliminar(p.getCategory());

        pilaRollback.push(new OperacionProducto("ELIMINAR", p));
        return true;
    }

    // ─────────────────────────────────────────
    // BÚSQUEDAS
    // ─────────────────────────────────────────

    public Productos buscarPorCodigo(String codigo) {
        return tablaHash.buscar(codigo);
    }

    public java.util.List<String> buscarPorRangoFecha(String desde, String hasta) {
        return arbolBFechas.buscarRango(desde, hasta);
    }

    public java.util.List<String> buscarPorCategoria(String categoria) {
        return arbolBPlusCategoria.buscarPorCategoria(categoria);
    }

    // ─────────────────────────────────────────
    // DESHACER (ROLLBACK)
    // ─────────────────────────────────────────

    public OperacionProducto deshacerUltimaOperacion() {
        if (pilaRollback.isEmpty()) return null;
        OperacionProducto op = pilaRollback.pop();

        if (op.getTipo().equals("AGREGAR")) {
            // Deshacer un agregar = eliminar
            lista.remove(op.getProducto());
            tablaHash.eliminar(op.getProducto().getBarCode());
        } else if (op.getTipo().equals("ELIMINAR")) {
            // Deshacer un eliminar = volver a agregar
            agregarProducto(op.getProducto());
        }
        return op;
    }

    // ─────────────────────────────────────────
    // SISTEMA DE DESPACHO
    // ─────────────────────────────────────────

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

    // ─────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────

    public int getIdSucursal() {
        return idSucursal;
    }

    public String getNameSucursal() {
        return nameSucursal;
    }

    public String getLocation() {
        return location;
    }

    public int getEntryTime() {
        return entryTime;
    }

    public int getTransferTime() {
        return transferTime;
    }

    public int getDispatchInterval() {
        return dispatchInterval;
    }

    public ListaEnlazada<Productos> getLista() {
        return lista;
    }

    public TablaHash getTablaHash() {
        return tablaHash;
    }

    public ArbolB<String> getArbolBFechas() {
        return arbolBFechas;
    }

    public ArbolBPlus<String> getArbolBPlusCategoria() {
        return arbolBPlusCategoria;
    }

    public Cola<Productos> getColaIngreso() {
        return colaIngreso;
    }

    public Cola<Productos> getColaTraspaso() {
        return colaTraspaso;
    }

    public Cola<Productos> getColaSalida() {
        return colaSalida;
    }

    @Override
    public String toString() {
        return idSucursal + " - " + nameSucursal + " (" + location + ")";
    }

}
