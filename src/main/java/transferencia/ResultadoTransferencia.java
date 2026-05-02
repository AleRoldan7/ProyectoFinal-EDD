package transferencia;

import estructuras.lista.ListaEnlazada;

import java.util.ArrayList;
import java.util.List;

public class ResultadoTransferencia {

    private boolean exito;
    private String mensaje;
    private ListaEnlazada<PasoTransferencia> pasos;
    private double totalSegundos;
    public int[] ruta;


    public ResultadoTransferencia() {
        this.pasos = new ListaEnlazada<>();
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public ListaEnlazada<PasoTransferencia> getPasos() {
        return pasos;
    }

    public void setPasos(ListaEnlazada<PasoTransferencia> pasos) {
        this.pasos = pasos;
    }

    public double getTotalSegundos() {
        return totalSegundos;
    }

    public void setTotalSegundos(double totalSegundos) {
        this.totalSegundos = totalSegundos;
    }

    public int[] getRuta() {
        return ruta;
    }

    public void setRuta(int[] ruta) {
        this.ruta = ruta;
    }
}
