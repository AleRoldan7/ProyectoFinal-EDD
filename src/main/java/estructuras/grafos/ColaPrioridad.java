package estructuras.grafos;

public class ColaPrioridad {

    // Entrada de la cola: nodo + su distancia acumulada
    public static class Entrada {
        public int nodo;
        public double distancia;

        public Entrada(int nodo, double distancia) {
            this.nodo = nodo;
            this.distancia = distancia;
        }
    }

    private Entrada[] heap; // arreglo que representa el heap
    private int size;
    private int capacidad;

    public ColaPrioridad(int capacidad) {
        this.capacidad = capacidad;
        this.heap = new Entrada[capacidad];
        this.size = 0;
    }

    // ─────────────────────────────────────────
    // POSICIONES EN EL HEAP
    // Para un nodo en índice i:
    //   padre     → (i-1) / 2
    //   hijo izq  → 2*i + 1
    //   hijo der  → 2*i + 2
    // ─────────────────────────────────────────

    private int padre(int i) {
        return (i - 1) / 2;
    }

    private int hijoIzq(int i) {
        return 2 * i + 1;
    }

    private int hijoDer(int i) {
        return 2 * i + 2;
    }

    private void intercambiar(int i, int j) {
        Entrada temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    // ─────────────────────────────────────────
    // INSERTAR — O(log n)
    // ─────────────────────────────────────────

    public void insertar(int nodo, double distancia) {
        if (size >= capacidad)
            throw new RuntimeException("Cola de prioridad llena");

        heap[size] = new Entrada(nodo, distancia);
        int i = size;
        size++;

        // Subir hasta que el padre sea menor (heapify up)
        while (i > 0 &&
                heap[i].distancia < heap[padre(i)].distancia) {
            intercambiar(i, padre(i));
            i = padre(i);
        }
    }

    // ─────────────────────────────────────────
    // EXTRAER MÍNIMO — O(log n)
    // ─────────────────────────────────────────

    public Entrada extraerMinimo() {
        if (isEmpty())
            throw new RuntimeException("Cola vacía");

        Entrada minimo = heap[0];

        // Mover el último elemento a la raíz
        heap[0] = heap[size - 1];
        size--;

        // Bajar hasta que los hijos sean mayores (heapify down)
        heapifyDown(0);

        return minimo;
    }

    private void heapifyDown(int i) {
        int menor = i;
        int izq = hijoIzq(i);
        int der = hijoDer(i);

        if (izq < size &&
                heap[izq].distancia < heap[menor].distancia)
            menor = izq;

        if (der < size &&
                heap[der].distancia < heap[menor].distancia)
            menor = der;

        if (menor != i) {
            intercambiar(i, menor);
            heapifyDown(menor);
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
