package estructuras.arbolBPlus;

import estructuras.lista.ListaEnlazada;

import java.util.ArrayList;
import java.util.List;

public class ArbolBPlus<T extends Comparable<T>> {

    private NodoBPlus<T> raiz;
    private int orden;

    public ArbolBPlus(int orden) {
        this.orden = orden;
        this.raiz = new NodoBPlus<T>(true, orden);
    }

    public void insertar(T clave) {

        if (raiz.getNumClaves() == orden - 1) {
            NodoBPlus<T> raizNueva = new NodoBPlus<T>(false, orden);
            raizNueva.getHijos()[0] = raiz;
            dividir(raizNueva, 0);
            raiz = raizNueva;
        }

        insertarNoLleno(raiz, clave);
    }

    private void insertarNoLleno(NodoBPlus<T> nodo, T clave) {

        if (nodo.isEsHoja()) {
            int i = nodo.getNumClaves() - 1;
            while (i >= 0 && clave.compareTo(nodo.getClaves()[i]) < 0) {
                nodo.getClaves()[i + 1] = nodo.getClaves()[i];
                i--;
            }
            nodo.getClaves()[i + 1] = clave;
            nodo.setNumClaves(nodo.getNumClaves() + 1);
        } else {
            int i = nodo.getNumClaves() - 1;
            while (i >= 0 && clave.compareTo(nodo.getClaves()[i]) < 0)
                i--;
            i++;
            if (nodo.getHijos()[i].getNumClaves() == orden - 1) {
                dividir(nodo, i);
                if (clave.compareTo(nodo.getClaves()[i]) > 0) i++;
            }
            insertarNoLleno(nodo.getHijos()[i], clave);
        }
    }

    private void dividir(NodoBPlus<T> padre, int idx) {
        NodoBPlus hijo = padre.getHijos()[idx];
        NodoBPlus nuevo = new NodoBPlus(hijo.isEsHoja(), orden);
        int medio = hijo.getNumClaves() / 2;

        if (hijo.isEsHoja()) {
            // En B+ las hojas conservan la clave media
            int count = 0;
            for (int i = medio; i < hijo.getNumClaves(); i++) {
                nuevo.getClaves()[count++] = hijo.getClaves()[i];
                hijo.getClaves()[i] = null;
            }
            nuevo.setNumClaves(count);
            hijo.setNumClaves(medio);


            // Enlazar hojas
            nuevo.setSiguiente(hijo.getSiguiente());
            hijo.setSiguiente(nuevo);

            // Subir al padre la primera clave del nuevo
            insertarEnPadre(padre, idx, (T) nuevo.getClaves()[0], nuevo);
        } else {
            T claveMedia = (T) hijo.getClaves()[medio];
            int count = 0;
            for (int i = medio + 1; i < hijo.getNumClaves(); i++)
                nuevo.getClaves()[count++] = hijo.getClaves()[i];
            nuevo.setNumClaves(count);

            count = 0;
            for (int i = medio + 1; i <= hijo.getNumClaves(); i++)
                nuevo.getHijos()[count++] = hijo.getHijos()[i];

            // Limpiar el hijo
            for (int i = medio; i < hijo.getNumClaves(); i++)
                hijo.getClaves()[i] = null;
            for (int i = medio + 1; i <= hijo.getNumClaves(); i++)
                hijo.getHijos()[i] = null;
            hijo.setNumClaves(medio);

            insertarEnPadre(padre, idx, claveMedia, nuevo);
        }
    }

    private void insertarEnPadre(NodoBPlus padre, int idx, T clave, NodoBPlus nuevo) {
        // Desplazar claves e hijos del padre
        for (int i = padre.getNumClaves(); i > idx; i--)
            padre.getClaves()[i] = padre.getClaves()[i - 1];
        for (int i = padre.getNumClaves() + 1; i > idx + 1; i--)
            padre.getHijos()[i] = padre.getHijos()[i - 1];

        padre.getClaves()[idx]     = clave;
        padre.getHijos()[idx + 1]  = nuevo;
        padre.setNumClaves(padre.getNumClaves() + 1);
    }


    public boolean buscar(T clave) {
        NodoBPlus<T> hoja = encontrarHoja(clave);
        for (int i = 0; i < hoja.getNumClaves(); i++) {
            if (hoja.getClaves()[i].compareTo(clave) == 0)
                return true;
        }
        return false;

    }

    private NodoBPlus encontrarHoja(T clave) {
        NodoBPlus actual = raiz;
        while (!actual.isEsHoja()) {
            int i = 0;
            while (i < actual.getNumClaves() && clave.compareTo((T) actual.getClaves()[i]) >= 0) i++;
            actual = actual.getHijos()[i];
        }
        return actual;
    }



    public ListaEnlazada<T> buscarPorCategoria(T categoria) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();

        NodoBPlus<T> hoja = encontrarHoja(categoria);

        while (hoja != null) {

            for (int i = 0; i < hoja.getNumClaves(); i++) {
                T clave = hoja.getClaves()[i];

                if (clave.compareTo(categoria) == 0) {
                    resultado.agregar(clave);
                }

                if (clave.compareTo(categoria) > 0) {
                    return resultado;
                }
            }
            hoja = hoja.getSiguiente();
        }
        return resultado;
    }


    public ListaEnlazada<T> buscarRango(T desde, T hasta) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        NodoBPlus<T> hoja = encontrarHoja(desde);

        while (hoja != null) {

            for (int i = 0; i < hoja.getNumClaves(); i++) {
                T clave = hoja.getClaves()[i];

                if (clave.compareTo(desde) >= 0 && clave.compareTo(hasta) <= 0) {
                    resultado.agregar(clave);
                }

                if (clave.compareTo(hasta) > 0) {
                    return resultado;
                }
            }
            hoja = hoja.getSiguiente();
        }
        return resultado;
    }


    //public void eliminar(T clave) {eliminar(raiz, clave);}

    private int minClaves() {
        return (int) Math.ceil((orden - 1) / 2.0);
    }

    public void eliminar(T clave) {
        eliminarRec(raiz, clave);

        // Si la raíz queda vacía y tiene hijo se baja un nivel
        if (!raiz.isEsHoja() && raiz.getNumClaves() == 0) {
            raiz = raiz.getHijos()[0];
        }
    }

    private void eliminarRec(NodoBPlus<T> nodo, T clave) {

        if (nodo.isEsHoja()) {
            eliminarEnHoja(nodo, clave);
            return;
        }

        int i = 0;
        while (i < nodo.getNumClaves() && clave.compareTo(nodo.getClaves()[i]) >= 0)
            i++;

        NodoBPlus<T> hijo = nodo.getHijos()[i];
        eliminarRec(hijo, clave);

        if (hijo.getNumClaves() < minClaves()) {
            rebalancear(nodo, i);
        }
    }

    private void eliminarEnHoja(NodoBPlus<T> nodo, T clave) {
        int i;

        for (i = 0; i < nodo.getNumClaves(); i++) {
            if (nodo.getClaves()[i].compareTo(clave) == 0) break;
        }

        if (i == nodo.getNumClaves()) return;

        for (int j = i; j < nodo.getNumClaves() - 1; j++) {
            nodo.getClaves()[j] = nodo.getClaves()[j + 1];
        }

        nodo.getClaves()[nodo.getNumClaves() - 1] = null;
        nodo.setNumClaves(nodo.getNumClaves() - 1);
    }

    private void rebalancear(NodoBPlus<T> padre, int idx) {

        NodoBPlus<T> hijo = padre.getHijos()[idx];

        NodoBPlus<T> izq = (idx > 0) ? padre.getHijos()[idx - 1] : null;
        NodoBPlus<T> der = (idx < padre.getNumClaves()) ? padre.getHijos()[idx + 1] : null;

        // 🔹 Intentar BORROW izquierda
        if (izq != null && izq.getNumClaves() > minClaves()) {
            prestarDeIzquierda(padre, idx, hijo, izq);
            return;
        }

        // 🔹 Intentar BORROW derecha
        if (der != null && der.getNumClaves() > minClaves()) {
            prestarDeDerecha(padre, idx, hijo, der);
            return;
        }
        if (izq != null) {
            merge(padre, idx - 1);
        } else {
            merge(padre, idx);
        }
    }

    private void prestarDeIzquierda(NodoBPlus<T> padre, int idx,
                                    NodoBPlus<T> hijo, NodoBPlus<T> izq) {

        // mover última clave de izquierda a hijo
        for (int i = hijo.getNumClaves(); i > 0; i--) {
            hijo.getClaves()[i] = hijo.getClaves()[i - 1];
        }

        hijo.getClaves()[0] = izq.getClaves()[izq.getNumClaves() - 1];

        hijo.setNumClaves(hijo.getNumClaves() + 1);
        izq.setNumClaves(izq.getNumClaves() - 1);

        // actualizar padre
        padre.getClaves()[idx - 1] = hijo.getClaves()[0];
    }

    private void prestarDeDerecha(NodoBPlus<T> padre, int idx,
                                  NodoBPlus<T> hijo, NodoBPlus<T> der) {

        hijo.getClaves()[hijo.getNumClaves()] = der.getClaves()[0];
        hijo.setNumClaves(hijo.getNumClaves() + 1);

        for (int i = 0; i < der.getNumClaves() - 1; i++) {
            der.getClaves()[i] = der.getClaves()[i + 1];
        }

        der.setNumClaves(der.getNumClaves() - 1);

        padre.getClaves()[idx] = der.getClaves()[0];
    }

    private void merge(NodoBPlus<T> padre, int idx) {

        NodoBPlus<T> izq = padre.getHijos()[idx];
        NodoBPlus<T> der = padre.getHijos()[idx + 1];

        // copiar claves
        for (int i = 0; i < der.getNumClaves(); i++) {
            izq.getClaves()[izq.getNumClaves() + i] = der.getClaves()[i];
        }

        izq.setNumClaves(izq.getNumClaves() + der.getNumClaves());

        // 🔥 mantener lista de hojas
        if (izq.isEsHoja()) {
            izq.setSiguiente(der.getSiguiente());
        }

        // eliminar referencia en padre
        for (int i = idx; i < padre.getNumClaves() - 1; i++) {
            padre.getClaves()[i] = padre.getClaves()[i + 1];
            padre.getHijos()[i + 1] = padre.getHijos()[i + 2];
        }

        padre.setNumClaves(padre.getNumClaves() - 1);
    }
    public ListaEnlazada<T> recorrerHojas() {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        NodoBPlus<T> hoja = raiz;

        while (!hoja.isEsHoja()) {
            hoja = hoja.getHijos()[0];
        }

        while (hoja != null) {
            for (int i = 0; i < hoja.getNumClaves(); i++) {
                resultado.agregar(hoja.getClaves()[i]);
            }
            hoja = hoja.getSiguiente();
        }
        return resultado;
    }

    public boolean isEmpty() {
        return raiz == null || raiz.getNumClaves() == 0;
    }

    public NodoBPlus<T> getRaiz() {
        return raiz;
    }
}

