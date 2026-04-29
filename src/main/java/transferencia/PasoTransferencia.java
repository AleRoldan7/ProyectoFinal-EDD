package transferencia;

public class PasoTransferencia {

    private int sucursaleID;
    private String nombreSucursal;
    private String etapa;
    private long tiempo;
    private String mensaje;

    public PasoTransferencia() {
    }

    public PasoTransferencia(int sucursaleID, String nombreSucursal, String etapa, long tiempo, String mensaje) {
        this.sucursaleID = sucursaleID;
        this.nombreSucursal = nombreSucursal;
        this.etapa = etapa;
        this.tiempo = tiempo;
        this.mensaje = mensaje;
    }

    public int getSucursaleID() {
        return sucursaleID;
    }

    public void setSucursaleID(int sucursaleID) {
        this.sucursaleID = sucursaleID;
    }

    public String getNombreSucursal() {
        return nombreSucursal;
    }

    public void setNombreSucursal(String nombreSucursal) {
        this.nombreSucursal = nombreSucursal;
    }

    public String getEtapa() {
        return etapa;
    }

    public void setEtapa(String etapa) {
        this.etapa = etapa;
    }

    public long getTiempo() {
        return tiempo;
    }

    public void setTiempo(long tiempo) {
        this.tiempo = tiempo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
