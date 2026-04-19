package estructuras.hash;

import clases.Productos;

public class TablaHash {

    private static final int CAPACIDADTABLA = 64;

    private NodoTabla[] buckets;
    private int size;

    public TablaHash() {
        buckets = new NodoTabla[CAPACIDADTABLA];
        size = 0;
    }

    private int hash(String clave) {
        return Math.abs(clave.hashCode()) % CAPACIDADTABLA;
    }

    // O(1) promedio
    public boolean insertar(String clave, Productos valor) {
        // Verificar duplicado primero (requerimiento del enunciado)
        if (contiene(clave)) {
            System.out.println("Código de barra duplicado: " + clave);
            return false;
        }

        int idx          = hash(clave);
        NodoTabla nueva    = new NodoTabla(clave, valor);
        nueva.siguiente  = buckets[idx]; // insertar al inicio del chain
        buckets[idx]     = nueva;
        size++;
        return true;
    }

    // ─────────────────────────────────────────
    // BUSCAR
    // ─────────────────────────────────────────

    // O(1) promedio — va directo al índice
    public Productos buscar(String clave) {
        int idx        = hash(clave);
        NodoTabla actual = buckets[idx];

        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                return actual.getProductos(); // encontrado
            }
            actual = actual.getSiguiente();
        }
        return null; // no existe
    }

    // ─────────────────────────────────────────
    // ELIMINAR
    // ─────────────────────────────────────────

    public boolean eliminar(String clave) {
        int indice = hash(clave);
        NodoTabla actual = buckets[indice];

        // Caso especial: eliminar el primero del bucket
        if (actual != null && actual.getClave().equals(clave)) {
            buckets[indice] = actual.getSiguiente();
            size--;
            return true;
        }

        // Buscar en el resto de la lista
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
    public int getCapacidad() { return CAPACIDADTABLA; }

    // Factor de carga — útil para visualizar en la UI
    // Si supera 0.75 hay muchas colisiones
    public double factorCarga() {
        return (double) size / CAPACIDADTABLA;
    }

    // Cuántos elementos tiene un bucket específico
    // Útil para visualizar colisiones en la UI
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
        for (int i = 0; i < CAPACIDADTABLA; i++) {
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

}
