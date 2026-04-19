package estructuras.grafos;

public class ResultadoRuta {
    private int[]  ruta;       // IDs de sucursales en orden
    private double pesoTotal;  // tiempo o costo total

    public ResultadoRuta(int[] ruta, double pesoTotal) {
        this.ruta      = ruta;
        this.pesoTotal = pesoTotal;
    }

    public boolean tieneRuta() {
        return ruta != null && ruta.length > 0;
    }

    public int[]  getRuta()       { return ruta; }
    public double getPesoTotal()  { return pesoTotal; }

    @Override
    public String toString() {
        if (!tieneRuta()) return "Sin ruta disponible";
        StringBuilder sb = new StringBuilder("Ruta: ");
        for (int i = 0; i < ruta.length; i++) {
            sb.append("Sucursal ").append(ruta[i]);
            if (i < ruta.length - 1) sb.append(" → ");
        }
        sb.append(" | Total: ").append(pesoTotal);
        return sb.toString();
    }
}