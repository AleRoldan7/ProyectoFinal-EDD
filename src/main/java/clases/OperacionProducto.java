package clases;

public class OperacionProducto {

    private String tipo;
    private Productos producto;
    private long timestamp;

    public OperacionProducto(String tipo, Productos producto) {
        this.tipo = tipo;
        this.producto = producto;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTipo() {
        return tipo;
    }

    public Productos getProducto() {
        return producto;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return tipo + " → " + producto.getName();
    }
}