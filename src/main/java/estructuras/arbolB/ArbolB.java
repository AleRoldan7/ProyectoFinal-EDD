package estructuras.arbolB;

import java.util.ArrayList;
import java.util.List;

public class ArbolB <T extends Comparable<T>> {

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

        // Si la raíz está llena, hay que dividirla
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

    // Inserta en un nodo que NO está lleno
    private void insertarNoLleno(NodoB<T> nodo, T clave) {
        int i = nodo.getClaves().size() - 1;

        if (nodo.isEsHoja()) {
            // Insertar en la posición correcta manteniendo orden
            nodo.getClaves().add(null); // hacer espacio
            while (i >= 0 && clave.compareTo(nodo.getClaves().get(i)) < 0) {
                nodo.getClaves().set(i + 1, nodo.getClaves().get(i));
                i--;
            }
            nodo.getClaves().set(i + 1, clave);
        } else {
            // Encontrar el hijo correcto
            while (i >= 0 && clave.compareTo(nodo.getClaves().get(i)) < 0) {
                i--;
            }
            i++;
            // Si el hijo está lleno, dividirlo primero
            if (nodo.getHijos().get(i).getClaves().size() == (2 * orden) - 1) {
                dividirHijo(nodo, i);
                if (clave.compareTo(nodo.getClaves().get(i)) > 0) {
                    i++;
                }
            }
            insertarNoLleno(nodo.getHijos().get(i), clave);
        }
    }

    // Divide el hijo i del nodo padre cuando está lleno
    private void dividirHijo(NodoB<T> padre, int i) {
        NodoB<T> hijoLleno = padre.getHijos().get(i);
        NodoB<T> nuevoHijo = new NodoB<>(hijoLleno.isEsHoja());

        // La clave media sube al padre
        T claveMedia = hijoLleno.getClaves().get(orden - 1);

        // Las claves de la derecha van al nuevo hijo
        for (int j = orden; j < (2 * orden) - 1; j++) {
            nuevoHijo.getClaves().add(hijoLleno.getClaves().get(j));
        }

        // Si no es hoja, los hijos de la derecha también se mueven
        if (!hijoLleno.isEsHoja()) {
            for (int j = orden; j < 2 * orden; j++) {
                nuevoHijo.getHijos().add(hijoLleno.getHijos().get(j));
            }
            // Eliminar hijos movidos del hijo lleno
            hijoLleno.getHijos().subList(orden, hijoLleno.getHijos().size()).clear();
        }

        // Eliminar claves movidas del hijo lleno (incluyendo la media)
        hijoLleno.getClaves().subList(orden - 1, hijoLleno.getClaves().size()).clear();

        // Insertar clave media en el padre
        padre.getClaves().add(i, claveMedia);

        // Insertar nuevo hijo en el padre
        padre.getHijos().add(i + 1, nuevoHijo);
    }


    public boolean buscar(T clave) {
        return buscar(raiz, clave);
    }

    private boolean buscar(NodoB<T> nodo, T clave) {
        int i = 0;

        // Avanzar mientras la clave sea mayor
        while (i < nodo.getClaves().size() &&
                clave.compareTo(nodo.getClaves().get(i)) > 0) {
            i++;
        }

        // Encontrado exacto
        if (i < nodo.getClaves().size() &&
                clave.compareTo(nodo.getClaves().get(i)) == 0) {
            return true;
        }

        // No encontrado y es hoja
        if (nodo.isEsHoja()) return false;

        // Bajar al hijo correspondiente
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
            // Si no es hoja, bajar por el hijo izquierdo primero
            if (!nodo.isEsHoja()) {
                buscarRango(nodo.getHijos().get(i), desde, hasta, resultado);
            }

            T clave = nodo.getClaves().get(i);

            // Si la clave está en el rango, agregarla
            if (clave.compareTo(desde) >= 0 && clave.compareTo(hasta) <= 0) {
                resultado.add(clave);
            }

            i++;
        }

        // Bajar por el último hijo
        if (!nodo.isEsHoja()) {
            buscarRango(nodo.getHijos().get(i), desde, hasta, resultado);
        }
    }

    // ─────────────────────────────────────────
    // ELIMINAR
    // ─────────────────────────────────────────

    public void eliminar(T clave) {
        if (raiz == null) return;
        eliminar(raiz, clave);

        // Si la raíz quedó vacía después de eliminar, su primer hijo es la nueva raíz
        if (raiz.getClaves().isEmpty() && !raiz.isEsHoja()) {
            raiz = raiz.getHijos().get(0);
        }
    }

    private void eliminar(NodoB<T> nodo, T clave) {
        int idx = encontrarIndice(nodo, clave);

        // La clave está en este nodo
        if (idx < nodo.getClaves().size() &&
                nodo.getClaves().get(idx).compareTo(clave) == 0) {

            if (nodo.isEsHoja()) {
                // Caso 1: nodo hoja → simplemente eliminar
                nodo.getClaves().remove(idx);
            } else {
                // Caso 2: nodo interno
                eliminarDeInterno(nodo, idx);
            }
        } else {
            // La clave no está en este nodo
            if (nodo.isEsHoja()) return; // no existe

            boolean estaEnUltimoHijo = (idx == nodo.getClaves().size());
            NodoB<T> hijo = nodo.getHijos().get(idx);

            // Si el hijo tiene pocas claves, rellenar antes de bajar
            if (hijo.getClaves().size() < orden) {
                rellenar(nodo, idx);
            }

            // Bajar al hijo correcto
            if (estaEnUltimoHijo &&
                    idx > nodo.getClaves().size()) {
                eliminar(nodo.getHijos().get(idx - 1), clave);
            } else {
                eliminar(nodo.getHijos().get(idx), clave);
            }
        }
    }

    // Encuentra el primer índice donde clave <= nodo.claves[i]
    private int encontrarIndice(NodoB<T> nodo, T clave) {
        int idx = 0;
        while (idx < nodo.getClaves().size() &&
                nodo.getClaves().get(idx).compareTo(clave) < 0) {
            idx++;
        }
        return idx;
    }

    // Eliminar de un nodo interno (tiene hijos)
    private void eliminarDeInterno(NodoB<T> nodo, int idx) {
        T clave = nodo.getClaves().get(idx);

        if (nodo.getHijos().get(idx).getClaves().size() >= orden) {
            // Caso 2a: el hijo izquierdo tiene suficientes claves
            // Reemplazar con el predecesor
            T predecesor = obtenerPredecesor(nodo, idx);
            nodo.getClaves().set(idx, predecesor);
            eliminar(nodo.getHijos().get(idx), predecesor);

        } else if (nodo.getHijos().get(idx + 1).getClaves().size() >= orden) {
            // Caso 2b: el hijo derecho tiene suficientes claves
            // Reemplazar con el sucesor
            T sucesor = obtenerSucesor(nodo, idx);
            nodo.getClaves().set(idx, sucesor);
            eliminar(nodo.getHijos().get(idx + 1), sucesor);

        } else {
            // Caso 2c: ambos hijos tienen pocas claves → fusionar
            fusionar(nodo, idx);
            eliminar(nodo.getHijos().get(idx), clave);
        }
    }

    // Obtener la clave más grande del subárbol izquierdo
    private T obtenerPredecesor(NodoB<T> nodo, int idx) {
        NodoB<T> actual = nodo.getHijos().get(idx);
        while (!actual.isEsHoja()) {
            actual = actual.getHijos().get(actual.getHijos().size() - 1);
        }
        return actual.getClaves().get(actual.getClaves().size() - 1);
    }

    // Obtener la clave más pequeña del subárbol derecho
    private T obtenerSucesor(NodoB<T> nodo, int idx) {
        NodoB<T> actual = nodo.getHijos().get(idx + 1);
        while (!actual.isEsHoja()) {
            actual = actual.getHijos().get(0);
        }
        return actual.getClaves().get(0);
    }

    // Fusionar hijo idx con hijo idx+1
    private void fusionar(NodoB<T> nodo, int idx) {
        NodoB<T> hijoIzq = nodo.getHijos().get(idx);
        NodoB<T> hijoDer = nodo.getHijos().get(idx + 1);

        // Bajar la clave del padre al hijo izquierdo
        hijoIzq.getClaves().add(nodo.getClaves().get(idx));

        // Mover todas las claves del hijo derecho al izquierdo
        hijoIzq.getClaves().addAll(hijoDer.getClaves());

        // Mover todos los hijos del derecho al izquierdo
        if (!hijoIzq.isEsHoja()) {
            hijoIzq.getHijos().addAll(hijoDer.getHijos());
        }

        // Eliminar la clave del padre y el hijo derecho
        nodo.getClaves().remove(idx);
        nodo.getHijos().remove(idx + 1);
    }

    // Rellenar hijo con pocas claves
    private void rellenar(NodoB<T> nodo, int idx) {
        if (idx != 0 &&
                nodo.getHijos().get(idx - 1).getClaves().size() >= orden) {
            // Tomar del hermano izquierdo
            tomarDelAnterior(nodo, idx);

        } else if (idx != nodo.getClaves().size() &&
                nodo.getHijos().get(idx + 1).getClaves().size() >= orden) {
            // Tomar del hermano derecho
            tomarDelSiguiente(nodo, idx);

        } else {
            // Fusionar
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

        // Mover clave del padre al inicio del hijo
        hijo.getClaves().add(0, nodo.getClaves().get(idx - 1));

        // Si no es hoja, mover el último hijo del hermano
        if (!hijo.isEsHoja()) {
            hijo.getHijos().add(0,
                    hermano.getHijos().remove(hermano.getHijos().size() - 1));
        }

        // Subir la última clave del hermano al padre
        nodo.getClaves().set(idx - 1,
                hermano.getClaves().remove(hermano.getClaves().size() - 1));
    }

    private void tomarDelSiguiente(NodoB<T> nodo, int idx) {
        NodoB<T> hijo = nodo.getHijos().get(idx);
        NodoB<T> hermano = nodo.getHijos().get(idx + 1);

        // Mover clave del padre al final del hijo
        hijo.getClaves().add(nodo.getClaves().get(idx));

        // Si no es hoja, mover el primer hijo del hermano
        if (!hijo.isEsHoja()) {
            hijo.getHijos().add(hermano.getHijos().remove(0));
        }

        // Subir la primera clave del hermano al padre
        nodo.getClaves().set(idx, hermano.getClaves().remove(0));
    }

    public boolean isEmpty() {
        return raiz.getClaves().isEmpty();
    }


}
