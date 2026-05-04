package estructuras.hash;

import clases.Productos;

public class TablaHash {

    private int capacidad;

    private static final double FACTOR_CARGA = 0.75;

    //private static final int CAPACIDADTABLA = 64;

    private NodoTabla[] buckets;
    private int size;

    public TablaHash() {
        this.capacidad = 64;
        this.buckets = new NodoTabla[capacidad];
        this.size = 0;
    }
    /*
    public TablaHash() {
        buckets = new NodoTabla[CAPACIDADTABLA];
        size = 0;
    }
     */

    /*
    private int hash(String clave) {
        return Math.abs(clave.hashCode()) % CAPACIDADTABLA;
    }
     */


    private int caluclarHash(String clave) {
        return Math.abs(clave.hashCode() % capacidad);
    }

    public boolean insertar(String clave, Productos valor) {

        if (contiene(clave)) {
            System.out.println("Se duplico el codigo de barra: " + clave);
            return false;
        }

        if (factorCarga() > FACTOR_CARGA) {
            redimensionarTabla();
        }

        int idTabla = caluclarHash(clave);
        NodoTabla nodoNuevo = new NodoTabla(clave, valor);

        nodoNuevo.siguiente = buckets[idTabla];
        buckets[idTabla] = nodoNuevo;

        size++;
        return true;
    }

    private void redimensionarTabla() {

        int capacidadNueva = capacidad * 2;
        NodoTabla[] nuevaTabla = new NodoTabla[capacidadNueva];

        NodoTabla[] tablaAnterior = buckets;

        capacidad = capacidadNueva;
        buckets = nuevaTabla;
        size = 0;

        for (NodoTabla nodoTabla: tablaAnterior) {

            while (nodoTabla != null) {
                insertar(nodoTabla.getClave(), nodoTabla.getProductos());
                nodoTabla = nodoTabla.getSiguiente();
            }
        }

        System.out.println("Se redimemnsiono la tablaaaaaaa: " + capacidad);
    }
    /*

    public boolean insertar(String clave, Productos valor) {
        if (contiene(clave)) {
            System.out.println("Código de barra duplicado: " + clave);
            return false;
        }

        int idx          = hash(clave);
        NodoTabla nueva    = new NodoTabla(clave, valor);
        nueva.siguiente  = buckets[idx];
        buckets[idx]     = nueva;
        size++;
        return true;
    }
  */

    public Productos buscar(String clave) {
        int idx        = caluclarHash(clave);
        NodoTabla actual = buckets[idx];

        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                return actual.getProductos(); // encontrado
            }
            actual = actual.getSiguiente();
        }
        return null;
    }



    public boolean eliminar(String clave) {
        int indice = caluclarHash(clave);
        NodoTabla actual = buckets[indice];

        if (actual != null && actual.getClave().equals(clave)) {
            buckets[indice] = actual.getSiguiente();
            size--;
            return true;
        }

        while (actual != null && actual.getSiguiente() != null) {
            if (actual.getSiguiente().getClave().equals(clave)) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                size--;
                return true;
            }
            actual = actual.getSiguiente();
        }

        return false;
    }



    public boolean contiene(String clave) {
        return buscar(clave) != null;
    }

    public int size()         { return size; }
    public boolean isEmpty()  { return size == 0; }
    public int getCapacidad() { return capacidad; }

    public double factorCarga() {
        return (double) size / capacidad;
    }

    public int tamanioBucket(int idx) {
        int count  = 0;
        NodoTabla e  = buckets[idx];
        while (e != null) {
            count++;
            e = e.siguiente;
        }
        return count;
    }

    public void imprimirTabla() {
        System.out.println("=== TABLA HASH ===");
        for (int i = 0; i < capacidad; i++) {
            if (buckets[i] != null) {
                System.out.print("Bucket " + i + ": ");
                NodoTabla actual = buckets[i];
                while (actual != null) {
                    System.out.print(actual.getClave() + " → ");
                    actual = actual.getSiguiente();
                }
                System.out.println("null");
            }
        }
        System.out.println("Total elementos: " + size);
    }


    public NodoTabla[] getBuckets() {
        return buckets;
    }

}
