package estructuras.arbolB;

import java.util.ArrayList;
import java.util.List;

public class ArbolB<T extends Comparable<T>> {

    private NodoB<T> raiz;
    private int orden; /* EL MAXIMO DE NODOS SE BASA EN 2T-1*/

    public ArbolB(int orden) {
        this.raiz = new NodoB<>(true);
        this.orden = orden;
    }

    public NodoB<T> getRaiz() {
        return raiz;
    }

    public void insertar(T clave) {
        NodoB<T> raizActual = raiz;

        if (raizActual.getClaves().size() == (2 * orden) - 1) {
            NodoB<T> nuevaRaiz = new NodoB<>(false);
            nuevaRaiz.getHijos().add(raizActual);
            dividirHijo(nuevaRaiz, 0);
            raiz = nuevaRaiz;
            insertarNoLleno(nuevaRaiz, clave);
        } else {
            insertarNoLleno(raizActual, clave);
        }
    }

    private void insertarNoLleno(NodoB<T> nodo, T clave) {
        int i = nodo.getClaves().size() - 1;

        if (nodo.isEsHoja()) {
            nodo.getClaves().add(null);
            while (i >= 0 && clave.compareTo(nodo.getClaves().get(i)) < 0) {
                nodo.getClaves().set(i + 1, nodo.getClaves().get(i));
                i--;
            }
            nodo.getClaves().set(i + 1, clave);
        } else {
            while (i >= 0 && clave.compareTo(nodo.getClaves().get(i)) < 0) {
                i--;
            }
            i++;
            if (nodo.getHijos().get(i).getClaves().size() == (2 * orden) - 1) {
                dividirHijo(nodo, i);
                if (clave.compareTo(nodo.getClaves().get(i)) > 0) {
                    i++;
                }
            }
            insertarNoLleno(nodo.getHijos().get(i), clave);
        }
    }

    private void dividirHijo(NodoB<T> padre, int i) {
        NodoB<T> hijoLleno = padre.getHijos().get(i);
        NodoB<T> nuevoHijo = new NodoB<>(hijoLleno.isEsHoja());

        T claveMedia = hijoLleno.getClaves().get(orden - 1);

        for (int j = orden; j < (2 * orden) - 1; j++) {
            nuevoHijo.getClaves().add(hijoLleno.getClaves().get(j));
        }

        if (!hijoLleno.isEsHoja()) {
            for (int j = orden; j < 2 * orden; j++) {
                nuevoHijo.getHijos().add(hijoLleno.getHijos().get(j));
            }
            hijoLleno.getHijos().subList(orden, hijoLleno.getHijos().size()).clear();
        }

        hijoLleno.getClaves().subList(orden - 1, hijoLleno.getClaves().size()).clear();

        padre.getClaves().add(i, claveMedia);

        padre.getHijos().add(i + 1, nuevoHijo);
    }


    public boolean buscar(T clave) {
        return buscar(raiz, clave);
    }

    private boolean buscar(NodoB<T> nodo, T clave) {
        int i = 0;

        while (i < nodo.getClaves().size() && clave.compareTo(nodo.getClaves().get(i)) > 0) {
            i++;
        }

        if (i < nodo.getClaves().size() && clave.compareTo(nodo.getClaves().get(i)) == 0) {
            return true;
        }

        if (nodo.isEsHoja()) return false;

        return buscar(nodo.getHijos().get(i), clave);
    }


    public List<T> buscarRango(T desde, T hasta) {
        List<T> resultado = new ArrayList<>();
        buscarRango(raiz, desde, hasta, resultado);
        return resultado;
    }

    private void buscarRango(NodoB<T> nodo, T desde, T hasta, List<T> resultado) {
        int i = 0;

        while (i < nodo.getClaves().size()) {
            if (!nodo.isEsHoja()) {
                buscarRango(nodo.getHijos().get(i), desde, hasta, resultado);
            }

            T clave = nodo.getClaves().get(i);

            if (clave.compareTo(desde) >= 0 && clave.compareTo(hasta) <= 0) {
                resultado.add(clave);
            }

            i++;
        }

        if (!nodo.isEsHoja()) {
            buscarRango(nodo.getHijos().get(i), desde, hasta, resultado);
        }
    }


    public void eliminar(T clave) {
        if (raiz == null) return;
        eliminar(raiz, clave);

        if (raiz.getClaves().isEmpty() && !raiz.isEsHoja()) {
            raiz = raiz.getHijos().get(0);
        }
    }

    private void eliminar(NodoB<T> nodo, T clave) {
        int idx = encontrarIndice(nodo, clave);

        if (idx < nodo.getClaves().size() && nodo.getClaves().get(idx).compareTo(clave) == 0) {

            if (nodo.isEsHoja()) {
                nodo.getClaves().remove(idx);
            } else {
                eliminarDeInterno(nodo, idx);
            }
        } else {
            if (nodo.isEsHoja()) return;

            boolean estaEnUltimoHijo = (idx == nodo.getClaves().size());
            NodoB<T> hijo = nodo.getHijos().get(idx);

            if (hijo.getClaves().size() < orden) {
                rellenar(nodo, idx);
            }

            if (estaEnUltimoHijo && idx > nodo.getClaves().size()) {
                eliminar(nodo.getHijos().get(idx - 1), clave);
            } else {
                eliminar(nodo.getHijos().get(idx), clave);
            }
        }
    }

    private int encontrarIndice(NodoB<T> nodo, T clave) {
        int idx = 0;
        while (idx < nodo.getClaves().size() && nodo.getClaves().get(idx).compareTo(clave) < 0) {
            idx++;
        }
        return idx;
    }

    private void eliminarDeInterno(NodoB<T> nodo, int idx) {
        T clave = nodo.getClaves().get(idx);

        if (nodo.getHijos().get(idx).getClaves().size() >= orden) {

            T predecesor = obtenerPredecesor(nodo, idx);
            nodo.getClaves().set(idx, predecesor);
            eliminar(nodo.getHijos().get(idx), predecesor);

        } else if (nodo.getHijos().get(idx + 1).getClaves().size() >= orden) {

            T sucesor = obtenerSucesor(nodo, idx);
            nodo.getClaves().set(idx, sucesor);
            eliminar(nodo.getHijos().get(idx + 1), sucesor);

        } else {
            fusionar(nodo, idx);
            eliminar(nodo.getHijos().get(idx), clave);
        }
    }

    private T obtenerPredecesor(NodoB<T> nodo, int idx) {
        NodoB<T> actual = nodo.getHijos().get(idx);
        while (!actual.isEsHoja()) {
            actual = actual.getHijos().get(actual.getHijos().size() - 1);
        }
        return actual.getClaves().get(actual.getClaves().size() - 1);
    }

    private T obtenerSucesor(NodoB<T> nodo, int idx) {
        NodoB<T> actual = nodo.getHijos().get(idx + 1);
        while (!actual.isEsHoja()) {
            actual = actual.getHijos().get(0);
        }
        return actual.getClaves().get(0);
    }

    private void fusionar(NodoB<T> nodo, int idx) {
        NodoB<T> hijoIzq = nodo.getHijos().get(idx);
        NodoB<T> hijoDer = nodo.getHijos().get(idx + 1);

        hijoIzq.getClaves().add(nodo.getClaves().get(idx));

        hijoIzq.getClaves().addAll(hijoDer.getClaves());

        if (!hijoIzq.isEsHoja()) {
            hijoIzq.getHijos().addAll(hijoDer.getHijos());
        }

        nodo.getClaves().remove(idx);
        nodo.getHijos().remove(idx + 1);
    }

    private void rellenar(NodoB<T> nodo, int idx) {
        if (idx != 0 && nodo.getHijos().get(idx - 1).getClaves().size() >= orden) {
            tomarDelAnterior(nodo, idx);

        } else if (idx != nodo.getClaves().size() && nodo.getHijos().get(idx + 1).getClaves().size() >= orden) {
            tomarDelSiguiente(nodo, idx);

        } else {
            if (idx != nodo.getClaves().size()) {
                fusionar(nodo, idx);
            } else {
                fusionar(nodo, idx - 1);
            }
        }
    }

    private void tomarDelAnterior(NodoB<T> nodo, int idx) {
        NodoB<T> hijo = nodo.getHijos().get(idx);
        NodoB<T> hermano = nodo.getHijos().get(idx - 1);

        hijo.getClaves().add(0, nodo.getClaves().get(idx - 1));

        if (!hijo.isEsHoja()) {
            hijo.getHijos().add(0, hermano.getHijos().remove(hermano.getHijos().size() - 1));
        }

        nodo.getClaves().set(idx - 1, hermano.getClaves().remove(hermano.getClaves().size() - 1));
    }

    private void tomarDelSiguiente(NodoB<T> nodo, int idx) {
        NodoB<T> hijo = nodo.getHijos().get(idx);
        NodoB<T> hermano = nodo.getHijos().get(idx + 1);

        hijo.getClaves().add(nodo.getClaves().get(idx));

        if (!hijo.isEsHoja()) {
            hijo.getHijos().add(hermano.getHijos().remove(0));
        }

        nodo.getClaves().set(idx, hermano.getClaves().remove(0));
    }

    public boolean isEmpty() {
        return raiz.getClaves().isEmpty();
    }


}
