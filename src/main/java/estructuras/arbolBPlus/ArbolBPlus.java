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

        // Si la raíz está llena, dividir
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
            // Insertar en orden en la hoja
            int i = 0;
            while (i < nodo.getClaves().size() &&
                    clave.compareTo(nodo.getClaves().get(i)) > 0) {
                i++;
            }
            nodo.getClaves().add(i, clave);

            // Si la hoja se llenó, dividir
            if (nodo.getClaves().size() >= orden) {
                // La división la maneja el padre —
                // esto se resuelve desde insertar()
            }
        } else {
            // Encontrar el hijo correcto
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
            // En B+ las hojas CONSERVAN la clave media
            // y se enlazan entre sí
            nuevoHijo.getClaves().addAll(hijo.getClaves().subList(mid, hijo.getClaves().size()));
            hijo.getClaves().subList(mid, hijo.getClaves().size()).clear();

            // Enlazar hojas
            nuevoHijo.siguiente = hijo.siguiente;
            hijo.siguiente = nuevoHijo;

            // La clave que sube al padre es la primera del nuevo hijo
            padre.getClaves().add(idx, nuevoHijo.getClaves().get(0));
        } else {
            // En nodos internos la clave media SUBE y no se repite
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

    // Navega hasta la hoja donde debería estar la clave
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

        // Ir a la hoja donde empieza la categoría
        NodoBPlus<T> hoja = encontrarHoja(categoria);

        // Recorrer las hojas enlazadas
        while (hoja != null) {
            for (T clave : hoja.getClaves()) {
                if (clave.compareTo(categoria) == 0) {
                    resultado.add(clave);
                }
                // Si ya pasamos la categoría, parar
                if (clave.compareTo(categoria) > 0) {
                    return resultado;
                }
            }
            hoja = hoja.siguiente; // avanzar a la siguiente hoja
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

        // Bajar hasta la primera hoja
        while (!hoja.isEsHoja()) {
            hoja = hoja.getHijos().get(0);
        }

        // Recorrer todas las hojas enlazadas
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

