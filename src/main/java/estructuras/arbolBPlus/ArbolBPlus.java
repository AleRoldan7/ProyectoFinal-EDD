package estructuras.arbolBPlus;

import java.util.ArrayList;
import java.util.List;

public class ArbolBPlus<T extends Comparable<T>> {

    private NodoBPlus<T> raiz;
    private int orden;

    public ArbolBPlus(int orden) {
        this.raiz = new NodoBPlus<>(true);
        this.orden = orden;
    }

    public void insertar(T clave) {
        NodoBPlus<T> raizActual = raiz;

        if (raizActual.getClaves().size() == orden - 1) {
            NodoBPlus<T> nuevaRaiz = new NodoBPlus<>(false);
            nuevaRaiz.getHijos().add(raizActual);
            dividir(nuevaRaiz, 0);
            raiz = nuevaRaiz;
        }
        insertarNoLleno(raiz, clave);
    }

    private void insertarNoLleno(NodoBPlus<T> nodo, T clave) {
        if (nodo.isEsHoja()) {
            int i = 0;
            while (i < nodo.getClaves().size() &&
                    clave.compareTo(nodo.getClaves().get(i)) > 0) {
                i++;
            }
            nodo.getClaves().add(i, clave);

            if (nodo.getClaves().size() >= orden) {

            }
        } else {
            int i = nodo.getClaves().size() - 1;
            while (i >= 0 && clave.compareTo(nodo.getClaves().get(i)) < 0) {
                i--;
            }
            i++;

            NodoBPlus<T> hijo = nodo.getHijos().get(i);
            if (hijo.getClaves().size() == orden - 1) {
                dividir(nodo, i);
                if (clave.compareTo(nodo.getClaves().get(i)) > 0) {
                    i++;
                }
            }
            insertarNoLleno(nodo.getHijos().get(i), clave);
        }
    }

    private void dividir(NodoBPlus<T> padre, int idx) {
        NodoBPlus<T> hijo = padre.getHijos().get(idx);
        NodoBPlus<T> nuevoHijo = new NodoBPlus<>(hijo.isEsHoja());
        int mid = hijo.getClaves().size() / 2;

        if (hijo.isEsHoja()) {

            nuevoHijo.getClaves().addAll(hijo.getClaves().subList(mid, hijo.getClaves().size()));
            hijo.getClaves().subList(mid, hijo.getClaves().size()).clear();

            nuevoHijo.siguiente = hijo.siguiente;
            hijo.siguiente = nuevoHijo;

            padre.getClaves().add(idx, nuevoHijo.getClaves().get(0));
        } else {
            T claveMedia = hijo.getClaves().get(mid);
            nuevoHijo.getClaves().addAll(hijo.getClaves().subList(mid + 1, hijo.getClaves().size()));
            hijo.getClaves().subList(mid, hijo.getClaves().size()).clear();

            nuevoHijo.getHijos().addAll(hijo.getHijos().subList(mid + 1, hijo.getHijos().size()));
            hijo.getHijos().subList(mid + 1, hijo.getHijos().size()).clear();

            padre.getClaves().add(idx, claveMedia);
        }

        padre.getHijos().add(idx + 1, nuevoHijo);
    }



    public boolean buscar(T clave) {
        NodoBPlus<T> hoja = encontrarHoja(clave);
        return hoja.getClaves().contains(clave);
    }

    private NodoBPlus<T> encontrarHoja(T clave) {
        NodoBPlus<T> actual = raiz;
        while (!actual.isEsHoja()) {
            int i = 0;
            while (i < actual.getClaves().size() &&
                    clave.compareTo(actual.getClaves().get(i)) >= 0) {
                i++;
            }
            actual = actual.getHijos().get(i);
        }
        return actual;
    }


    public List<T> buscarPorCategoria(T categoria) {
        List<T> resultado = new ArrayList<>();

        NodoBPlus<T> hoja = encontrarHoja(categoria);

        while (hoja != null) {
            for (T clave : hoja.getClaves()) {
                if (clave.compareTo(categoria) == 0) {
                    resultado.add(clave);
                }
                if (clave.compareTo(categoria) > 0) {
                    return resultado;
                }
            }
            hoja = hoja.siguiente;
        }
        return resultado;
    }


    public List<T> buscarRango(T desde, T hasta) {
        List<T> resultado = new ArrayList<>();
        NodoBPlus<T> hoja = encontrarHoja(desde);

        while (hoja != null) {
            for (T clave : hoja.getClaves()) {
                if (clave.compareTo(desde) >= 0 &&
                        clave.compareTo(hasta) <= 0) {
                    resultado.add(clave);
                }
                if (clave.compareTo(hasta) > 0) {
                    return resultado;
                }
            }
            hoja = hoja.siguiente;
        }
        return resultado;
    }


    public void eliminar(T clave) {
        eliminar(raiz, clave);
    }

    private void eliminar(NodoBPlus<T> nodo, T clave) {
        if (nodo.isEsHoja()) {
            nodo.getClaves().remove(clave);
            return;
        }

        int i = 0;
        while (i < nodo.getClaves().size() &&
                clave.compareTo(nodo.getClaves().get(i)) >= 0) {
            i++;
        }
        eliminar(nodo.getHijos().get(i), clave);
    }



    public List<T> recorrerHojas() {
        List<T> resultado = new ArrayList<>();
        NodoBPlus<T> hoja = raiz;

        while (!hoja.isEsHoja()) {
            hoja = hoja.getHijos().get(0);
        }

        while (hoja != null) {
            resultado.addAll(hoja.getClaves());
            hoja = hoja.siguiente;
        }
        return resultado;
    }

    public boolean isEmpty() {
        return raiz.getClaves().isEmpty();
    }

    public NodoBPlus<T> getRaiz() {
        return raiz;
    }
}

