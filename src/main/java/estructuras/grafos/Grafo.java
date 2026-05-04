package estructuras.grafos;


import java.io.BufferedReader;
import java.io.FileReader;

public class Grafo {

    private static final int NODOS_MAX = 1000;
    private NodoGrafo[] nodoGrafos;
    private int totalNodos;

    public Grafo() {
        this.nodoGrafos = new NodoGrafo[NODOS_MAX];
        this.totalNodos = 0;
    }

    private int buscarIndice(int idSucursal) {

        for (int i = 0; i < totalNodos; i++) {
            if (nodoGrafos[i].getIdSucursal() == idSucursal) {
                return i;
            }
        }
        return -1;
    }

    public void agregarSucursal(int idSucursal) {

        if (buscarIndice(idSucursal) == -1) {
            nodoGrafos[totalNodos] = new NodoGrafo(idSucursal);
            totalNodos++;
        }
    }

    private boolean existeConexion(int origen, int destino) {
        int idx = buscarIndice(origen);
        if (idx == -1) return false;

        NodoArista aux = nodoGrafos[idx].getListaArista();

        while (aux != null) {
            if (aux.getDestino() == destino) return true;
            aux = aux.getSiguiente();
        }
        return false;
    }

    public void agregarConexion(int origen, int destino, double tiempo, double costo) {

        if (existeConexion(origen, destino)) return;
        if (origen == destino) return;

        agregarSucursal(origen);
        agregarSucursal(destino);

        int idx = buscarIndice(origen);

        NodoArista nuevaArista = new NodoArista(destino, tiempo, costo);
        nuevaArista.setSiguiente(nodoGrafos[idx].getListaArista());
        nodoGrafos[idx].setListaArista(nuevaArista);
    }

    public void agregarConexionBiDireccional(int origen, int destino, double tiempo, double costo) {

        agregarConexion(origen, destino, tiempo, costo);
        agregarConexion(destino, origen, tiempo, costo);
    }


    /* ALGORITMO DE DIJKSTRA */

    public ResultadoRuta determinaDijkstra(int origen, int destino, boolean usarTiempo) {
        double[] distancia = new double[NODOS_MAX];
        int[] anterior = new int[NODOS_MAX];
        boolean[] visitado = new boolean[NODOS_MAX];

        for (int i = 0; i < NODOS_MAX; i++) {
            distancia[i] = Double.MAX_VALUE;
            anterior[i] = -1;
            visitado[i] = false;
        }

        int idxOrigen = buscarIndice(origen);
        if (idxOrigen == -1) return new ResultadoRuta(null, -1);

        distancia[idxOrigen] = 0;

        ColaPrioridad colaPrioridad = new ColaPrioridad(NODOS_MAX * NODOS_MAX);
        colaPrioridad.insertar(idxOrigen, 0);

        while (!colaPrioridad.isEmpty()) {
            ColaPrioridad.Entrada actual = colaPrioridad.extraerMinimo();
            int idxActual = actual.nodo;

            if (visitado[idxActual]) continue;
            visitado[idxActual] = true;

            if (nodoGrafos[idxActual].getIdSucursal() == destino) break;

            NodoArista arista = nodoGrafos[idxActual].getListaArista();
            while (arista != null) {
                int idxVecino = buscarIndice(arista.getDestino());
                if (idxVecino == -1) {
                    arista = arista.getSiguiente();
                    continue;
                }

                double peso = usarTiempo ? arista.getTiempo() : arista.getCosto();
                double nuevaDist = distancia[idxActual] + peso;

                if (nuevaDist < distancia[idxVecino]) {
                    distancia[idxVecino] = nuevaDist;
                    anterior[idxVecino] = idxActual;
                    colaPrioridad.insertar(idxVecino, nuevaDist);
                }

                arista = arista.getSiguiente();
            }
        }

        int idxDestino = buscarIndice(destino);
        if (idxDestino == -1 || distancia[idxDestino] == Double.MAX_VALUE) {
            return new ResultadoRuta(null, -1);
        }

        int[] caminoIdx = new int[NODOS_MAX];
        int largoCamino = 0;
        int paso = idxDestino;

        while (paso != -1) {
            caminoIdx[largoCamino] = nodoGrafos[paso].getIdSucursal();
            largoCamino++;
            paso = anterior[paso];
        }

        int[] ruta = new int[largoCamino];
        for (int i = 0; i < largoCamino; i++) {
            ruta[i] = caminoIdx[largoCamino - 1 - i];
        }

        return new ResultadoRuta(ruta, distancia[idxDestino]);
    }

    public void cargarConexionesCSV(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {

            String linea;
            br.readLine();
            int numLinea = 1;

            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] partes = linea.split(",");
                try {
                    int origen = Integer.parseInt(partes[0].trim());
                    int destino = Integer.parseInt(partes[1].trim());
                    double tiempo = Double.parseDouble(partes[2].trim());
                    double costo = Double.parseDouble(partes[3].trim());

                    agregarConexionBiDireccional(origen, destino, tiempo, costo);
                } catch (Exception e) {
                    System.err.println("Conexión inválida línea " + numLinea);
                }
            }
            System.out.println("Grafo cargado: " + totalNodos + " sucursales");

        } catch (java.io.IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public int getTotalNodos() {
        return totalNodos;
    }

    public NodoGrafo[] getNodos() {
        return nodoGrafos;
    }

    public int getIdSucursal(int idx) {
        return nodoGrafos[idx].getIdSucursal();
    }

    public NodoArista getAristas(int idx) {
        return nodoGrafos[idx].getListaArista();

    }

    public NodoArista getAdyacentesPorId(int idSucursal) {
        int idx = buscarIndice(idSucursal);
        if (idx == -1) return null;
        return nodoGrafos[idx].getListaArista();
    }

    public String obtenerConexionesComoTexto() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < totalNodos; i++) {

            int origen = nodoGrafos[i].getIdSucursal();
            sb.append("Sucursal ").append(origen).append(" → ");

            NodoArista aux = nodoGrafos[i].getListaArista();

            if (aux == null) {
                sb.append("sin conexiones\n");
            } else {
                while (aux != null) {
                    sb.append(aux.getDestino()).append("(T:").append(aux.getTiempo()).append(", C:").append(aux.getCosto()).append(")  ");

                    aux = aux.getSiguiente();
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
