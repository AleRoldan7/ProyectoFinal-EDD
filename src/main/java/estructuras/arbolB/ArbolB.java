package estructuras.arbolB;


import estructuras.lista.ListaEnlazada;

public class ArbolB<T extends Comparable<T>> {

    private NodoB raiz;
    private int orden;

    public ArbolB(int orden) {
        this.orden = orden;
        this.raiz = new NodoB(true, orden);
    }

    public void insertar(T clave) {
        NodoB raizActual = raiz;
        if (raizActual.getNumClaves() == 2 * orden - 1) {
            NodoB nuevaRaiz = new NodoB(false, orden);
            nuevaRaiz.getHijos()[0] = raizActual;
            dividirHijo(nuevaRaiz, 0);
            raiz = nuevaRaiz;
            insertarNoLleno(nuevaRaiz, clave);
        } else {
            insertarNoLleno(raizActual, clave);
        }
    }

    private void insertarNoLleno(NodoB nodo, T clave) {
        int i = nodo.getNumClaves() - 1;
        if (nodo.isEsHoja()) {
            while (i >= 0 && clave.compareTo((T) nodo.getClaves()[i]) < 0) {
                nodo.getClaves()[i + 1] = nodo.getClaves()[i];
                i--;
            }
            nodo.getClaves()[i + 1] = clave;
            nodo.setNumClaves(nodo.getNumClaves() + 1);
        } else {
            while (i >= 0 && clave.compareTo((T) nodo.getClaves()[i]) < 0) i--;
            i++;
            if (nodo.getHijos()[i].getNumClaves() == 2 * orden - 1) {
                dividirHijo(nodo, i);
                if (clave.compareTo((T) nodo.getClaves()[i]) > 0) i++;
            }
            insertarNoLleno(nodo.getHijos()[i], clave);
        }
    }

    private void dividirHijo(NodoB<T> padre, int i) {
        NodoB<T> lleno = padre.getHijos()[i];
        NodoB<T> nuevo = new NodoB<>(lleno.isEsHoja(), orden);

        nuevo.setNumClaves(orden - 1);

        // Copiar mitad derecha
        for (int j = 0; j < orden - 1; j++) {
            nuevo.getClaves()[j] = lleno.getClaves()[j + orden];
        }

        if (!lleno.isEsHoja()) {
            for (int j = 0; j < orden; j++) {
                nuevo.getHijos()[j] = lleno.getHijos()[j + orden];
            }
        }

        lleno.setNumClaves(orden - 1);

        // Mover hijos del padre
        for (int j = padre.getNumClaves(); j >= i + 1; j--) {
            padre.getHijos()[j + 1] = padre.getHijos()[j];
        }

        padre.getHijos()[i + 1] = nuevo;

        // Mover claves del padre
        for (int j = padre.getNumClaves() - 1; j >= i; j--) {
            padre.getClaves()[j + 1] = padre.getClaves()[j];
        }

        padre.getClaves()[i] = lleno.getClaves()[orden - 1];

        padre.setNumClaves(padre.getNumClaves() + 1);
    }

    public boolean buscar(T clave) {
        return buscar(raiz, clave);
    }

    private boolean buscar(NodoB<T> nodo, T clave) {
        int i = 0;

        while (i < nodo.getNumClaves() && clave.compareTo(nodo.getClaves()[i]) > 0) {
            i++;
        }

        if (i < nodo.getNumClaves() && clave.compareTo(nodo.getClaves()[i]) == 0) {
            return true;
        }

        if (nodo.isEsHoja()) return false;

        return buscar(nodo.getHijos()[i], clave);
    }

    public ListaEnlazada<T> buscarRango(T desde, T hasta) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        buscarRango(raiz, desde, hasta, resultado);
        return resultado;
    }

    private void buscarRango(NodoB<T> nodo, T desde, T hasta, ListaEnlazada<T> resultado) {

        if (nodo == null) return;

        int i = 0;

        while (i < nodo.getNumClaves()) {

            if (!nodo.isEsHoja()) {
                buscarRango(nodo.getHijos()[i], desde, hasta, resultado);
            }

            T clave = nodo.getClaves()[i];

            if (clave.compareTo(desde) >= 0 && clave.compareTo(hasta) <= 0) {
                resultado.agregar(clave);
            }

            i++;
        }

        if (!nodo.isEsHoja()) {
            buscarRango(nodo.getHijos()[i], desde, hasta, resultado);
        }
    }


    public void eliminar(T clave) {
        if (raiz == null || raiz.getNumClaves() == 0) return;
        eliminar(raiz, clave);
        if (raiz.getNumClaves() == 0 && !raiz.isEsHoja()) raiz = raiz.getHijos()[0];
    }

    private void eliminar(NodoB nodo, T clave) {
        int idx = encontrarIndice(nodo, clave);

        if (idx < nodo.getNumClaves() && nodo.getClaves()[idx].compareTo(clave) == 0) {
            if (nodo.isEsHoja()) {
                // Caso 1: hoja — eliminar directamente
                for (int i = idx; i < nodo.getNumClaves() - 1; i++)
                    nodo.getClaves()[i] = nodo.getClaves()[i + 1];
                nodo.getClaves()[nodo.getNumClaves() - 1] = null;
                nodo.setNumClaves(nodo.getNumClaves() - 1);
            } else {
                eliminarDeInterno(nodo, idx);
            }
        } else {
            if (nodo.isEsHoja()) return;
            boolean enUltimo = (idx == nodo.getNumClaves());
            if (nodo.getHijos()[idx].getNumClaves() < orden) rellenar(nodo, idx);
            if (enUltimo && idx > nodo.getNumClaves()) eliminar(nodo.getHijos()[idx - 1], clave);
            else eliminar(nodo.getHijos()[idx], clave);
        }
    }

    private int encontrarIndice(NodoB nodo, T clave) {
        int idx = 0;
        while (idx < nodo.getNumClaves() && nodo.getClaves()[idx].compareTo(clave) < 0) idx++;
        return idx;
    }

    private void eliminarDeInterno(NodoB nodo, int idx) {
        T clave = (T) nodo.getClaves()[idx];
        if (nodo.getHijos()[idx].getNumClaves() >= orden) {
            T pred = obtenerPredecesor(nodo, idx);
            nodo.getClaves()[idx] = pred;
            eliminar(nodo.getHijos()[idx], pred);
        } else if (nodo.getHijos()[idx + 1].getNumClaves() >= orden) {
            T suc = obtenerSucesor(nodo, idx);
            nodo.getClaves()[idx] = suc;
            eliminar(nodo.getHijos()[idx + 1], suc);
        } else {
            fusionar(nodo, idx);
            eliminar(nodo.getHijos()[idx], clave);
        }
    }

    private T obtenerPredecesor(NodoB nodo, int idx) {
        NodoB actual = nodo.getHijos()[idx];
        while (!actual.isEsHoja()) actual = actual.getHijos()[actual.getNumClaves()];
        return (T) actual.getClaves()[actual.getNumClaves() - 1];
    }

    private T obtenerSucesor(NodoB nodo, int idx) {
        NodoB actual = nodo.getHijos()[idx + 1];
        while (!actual.isEsHoja()) actual = actual.getHijos()[0];
        return (T) actual.getClaves()[0];
    }

    private void fusionar(NodoB nodo, int idx) {
        NodoB izq = nodo.getHijos()[idx];
        NodoB der = nodo.getHijos()[idx + 1];

        izq.getClaves()[orden - 1] = nodo.getClaves()[idx];

        for (int i = 0; i < der.getNumClaves(); i++)
            izq.getClaves()[i + orden] = der.getClaves()[i];

        if (!izq.isEsHoja()) {
            for (int i = 0; i <= der.getNumClaves(); i++)
                izq.getHijos()[i + orden] = der.getHijos()[i];
        }
        izq.setNumClaves(2 * orden - 1);

        for (int i = idx; i < nodo.getNumClaves() - 1; i++)
            nodo.getClaves()[i] = nodo.getClaves()[i + 1];
        nodo.getClaves()[nodo.getNumClaves() - 1] = null;

        for (int i = idx + 1; i < nodo.getNumClaves(); i++)
            nodo.getHijos()[i] = nodo.getHijos()[i + 1];
        nodo.getHijos()[nodo.getNumClaves()] = null;
        nodo.setNumClaves(nodo.getNumClaves() - 1);
    }

    private void rellenar(NodoB nodo, int idx) {
        if (idx != 0 && nodo.getHijos()[idx - 1].getNumClaves() >= orden) tomarDelAnterior(nodo, idx);
        else if (idx != nodo.getNumClaves() && nodo.getHijos()[idx + 1].getNumClaves() >= orden) tomarDelSiguiente(nodo, idx);
        else {
            if (idx != nodo.getNumClaves()) fusionar(nodo, idx);
            else fusionar(nodo, idx - 1);
        }
    }

    private void tomarDelAnterior(NodoB nodo, int idx) {
        NodoB hijo = nodo.getHijos()[idx];
        NodoB hermano = nodo.getHijos()[idx - 1];

        for (int i = hijo.getNumClaves() - 1; i >= 0; i--)
            hijo.getClaves()[i + 1] = hijo.getClaves()[i];

        if (!hijo.isEsHoja()) {
            for (int i = hijo.getNumClaves(); i >= 0; i--)
                hijo.getHijos()[i + 1] = hijo.getHijos()[i];
        }

        hijo.getClaves()[0] = nodo.getClaves()[idx - 1];

        if (!hijo.isEsHoja()) hijo.getHijos()[0] = hermano.getHijos()[hermano.getNumClaves()];

        nodo.getClaves()[idx - 1] = hermano.getClaves()[hermano.getNumClaves() - 1];
        hermano.getClaves()[hermano.getNumClaves() - 1] = null;
        if (!hermano.isEsHoja()) hermano.getHijos()[hermano.getNumClaves()] = null;
        hermano.setNumClaves(hermano.getNumClaves() - 1);
        hijo.setNumClaves(hijo.getNumClaves() + 1);
    }

    private void tomarDelSiguiente(NodoB nodo, int idx) {
        NodoB hijo = nodo.getHijos()[idx];
        NodoB hermano = nodo.getHijos()[idx + 1];

        hijo.getClaves()[hijo.getNumClaves()] = nodo.getClaves()[idx];

        if (!hijo.isEsHoja()) hijo.getHijos()[hijo.getNumClaves() + 1] = hermano.getHijos()[0];

        nodo.getClaves()[idx] = hermano.getClaves()[0];

        for (int i = 0; i < hermano.getNumClaves() - 1; i++)
            hermano.getClaves()[i] = hermano.getClaves()[i + 1];
        hermano.getClaves()[hermano.getNumClaves() - 1] = null;

        if (!hermano.isEsHoja()) {
            for (int i = 0; i < hermano.getNumClaves(); i++)
                hermano.getHijos()[i] = hermano.getHijos()[i + 1];
            hermano.getHijos()[hermano.getNumClaves()] = null;
        }
        hermano.setNumClaves(hermano.getNumClaves() - 1);
        hijo.setNumClaves(hijo.getNumClaves() + 1);
    }

    public boolean isEmpty() {
        return raiz == null || raiz.getNumClaves() == 0;
    }

    public NodoB getRaiz() {
        return raiz;
    }
}
