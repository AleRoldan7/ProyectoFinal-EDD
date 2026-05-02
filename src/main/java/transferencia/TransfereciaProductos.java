package transferencia;

import clases.Productos;
import clases.Sucursal;
import estructuras.grafos.Grafo;
import estructuras.grafos.ResultadoRuta;
import ui.view.AppState;
import Enum.Estado;

public class TransfereciaProductos {

    private AppState state;

    private PasoTransferencia pasoTransferencia = new PasoTransferencia();
    private ResultadoTransferencia resultadoTransferencia = new ResultadoTransferencia();

    public TransfereciaProductos(AppState state) {
        this.state = state;
    }

    public ResultadoTransferencia transferencia(String codigoProducto, int cantidad, int idOrigen, int idDestino, boolean usarTiempo) {

        resultadoTransferencia = new ResultadoTransferencia();

        Sucursal origen = state.getCargaCSV().buscarSucursal(idOrigen);
        if (origen == null) {
            resultadoTransferencia.setExito(false);
            resultadoTransferencia.setMensaje("Sucursal origen no existe");
            return resultadoTransferencia;
        }

        Productos productoBase = origen.buscarPorCodigo(codigoProducto);
        if (productoBase == null) {
            resultadoTransferencia.setExito(false);
            resultadoTransferencia.setMensaje("Producto no existe en origen");
            return resultadoTransferencia;
        }

        if (productoBase.getStock() < cantidad) {
            resultadoTransferencia.setExito(false);
            resultadoTransferencia.setMensaje("Stock insuficiente");
            return resultadoTransferencia;
        }

        if (idOrigen == idDestino) {
            resultadoTransferencia.setExito(false);
            resultadoTransferencia.setMensaje("Origen y destino son iguales");
            return resultadoTransferencia;
        }

        Sucursal destino = state.getCargaCSV().buscarSucursal(idDestino);
        if (destino == null) {
            resultadoTransferencia.setExito(false);
            resultadoTransferencia.setMensaje("Sucursal destino no existe");
            return resultadoTransferencia;
        }

        ResultadoRuta ruta = state.getGrafo().determinaDijkstra(idOrigen, idDestino, usarTiempo);

        if (!ruta.tieneRuta()) {
            resultadoTransferencia.setExito(false);
            resultadoTransferencia.setMensaje("No hay ruta disponible");
            return resultadoTransferencia;
        }

        int[] rutaIds = ruta.getRuta();
        resultadoTransferencia.setRuta(rutaIds);

        for (int i = 0; i < cantidad; i++) {

            Productos unidad = new Productos(
                    productoBase.getBranchId(),
                    productoBase.getName(),
                    productoBase.getBarCode(),
                    productoBase.getCategory(),
                    productoBase.getExpiryDate(),
                    productoBase.getBrand(),
                    productoBase.getPrice(),
                    1
            );

            unidad.setStatus(Estado.TRANSITO);

            procesarUnidad(unidad, rutaIds, usarTiempo);
        }

        productoBase.setStock(productoBase.getStock() - cantidad);

        if (productoBase.getStock() == 0) {
            origen.eliminarProducto(codigoProducto);
        }

        resultadoTransferencia.setExito(true);
        resultadoTransferencia.setMensaje(
                cantidad + " unidades transferidas correctamente"
        );

        return resultadoTransferencia;
    }

    private void procesarUnidad(Productos p, int[] rutaIds, boolean usarTiempo) {

        double eta = 0;

        for (int i = 0; i < rutaIds.length; i++) {

            Sucursal suc = state.getCargaCSV().buscarSucursal(rutaIds[i]);
            if (suc == null) continue;

            boolean esDestino = (i == rutaIds.length - 1);
            boolean esIntermedia = (i > 0 && i < rutaIds.length - 1);

            suc.recibirProducto(p);
            eta += suc.getEntryTime();

            resultadoTransferencia.getPasos().agregar(new PasoTransferencia(suc.getIdSucursal(), suc.getNameSucursal(), "INGRESO", suc.getEntryTime() * 1000, "Producto recibido"));

            suc.getColaIngreso().dequeue();

            if (esIntermedia) {

                suc.prepararTraspaso(p);
                eta += suc.getTransferTime();

                resultadoTransferencia.getPasos().agregar(new PasoTransferencia(suc.getIdSucursal(), suc.getNameSucursal(), "TRASPASO", suc.getTransferTime() * 1000, "Preparando envío"));

                suc.getColaTraspaso().dequeue();
            }

            if (!esDestino) {

                suc.alistarSalida(p);
                eta += suc.getDispatchInterval();

                resultadoTransferencia.getPasos().agregar(new PasoTransferencia(suc.getIdSucursal(), suc.getNameSucursal(), "SALIDA", suc.getDispatchInterval() * 1000, "Producto enviado"));

                suc.despachar();

                double peso = obtenerPeso(rutaIds[i], rutaIds[i + 1], usarTiempo);

                eta += peso;
            }

            if (esDestino) {

                p.setStatus(Estado.DISPONIBLE);
                p.setBranchId(suc.getIdSucursal());

                Productos existente = suc.buscarPorCodigo(p.getBarCode());

                if (existente != null) {
                    existente.setStock(existente.getStock() + 1);
                } else {
                    suc.agregarProducto(p);
                }

                resultadoTransferencia.getPasos().agregar(new PasoTransferencia(suc.getIdSucursal(), suc.getNameSucursal(), "DESTINO", 0, "Producto recibido en destino"));
            }
        }

        resultadoTransferencia.setTotalSegundos(resultadoTransferencia.getTotalSegundos() + eta);
    }

    private double obtenerPeso(int origen, int destino, boolean usarTiempo) {
        Grafo grafo = state.getGrafo();
        for (int i = 0; i < grafo.getTotalNodos(); i++) {
            if (grafo.getIdSucursal(i) == origen) {
                estructuras.grafos.NodoArista a = grafo.getAristas(i);
                while (a != null) {
                    if (a.getDestino() == destino) {
                        return usarTiempo ? a.getTiempo() : a.getCosto();
                    }
                    a = a.getSiguiente();
                }
            }
        }
        return 0;
    }
}