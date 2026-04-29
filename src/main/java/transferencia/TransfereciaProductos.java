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

    public ResultadoTransferencia transferencia(String codigoProducto, int idOrigen, int idDestino, boolean usarTiempo) {

        Sucursal origen = state.getCargaCSV().buscarSucursal(idOrigen);

        if (origen == null) {
            boolean exito = false;
            resultadoTransferencia.setExito(exito);
            resultadoTransferencia.isExito();

            String mensaje = "La Sucursal no existe: " + idOrigen;
            resultadoTransferencia.setMensaje(mensaje);
            resultadoTransferencia.getMensaje();

            return resultadoTransferencia;
        }

        Productos productos = origen.buscarPorCodigo(codigoProducto);

        if (productos == null) {

            boolean exito = false;
            resultadoTransferencia.setExito(exito);
            resultadoTransferencia.isExito();

            String mensaje = "Producto no encontrado en: " + origen.getNameSucursal() + ": " + codigoProducto;
            resultadoTransferencia.setMensaje(mensaje);
            resultadoTransferencia.getMensaje();

            return resultadoTransferencia;
        }

        Sucursal destino = state.getCargaCSV().buscarSucursal(idDestino);

        if (destino == null) {
            boolean exito = false;
            resultadoTransferencia.setExito(exito);
            resultadoTransferencia.isExito();

            String mensaje = "La Sucursal destino no existe: " + idDestino;
            resultadoTransferencia.setMensaje(mensaje);
            resultadoTransferencia.getMensaje();

            return resultadoTransferencia;
        }

        if (idOrigen == idDestino) {
            boolean exito = false;
            resultadoTransferencia.setExito(exito);
            resultadoTransferencia.isExito();

            String mensaje = "El origen y el destino de sucursal son lo mismo: ";
            resultadoTransferencia.setMensaje(mensaje);
            resultadoTransferencia.getMensaje();

            return resultadoTransferencia;
        }

        ResultadoRuta ruta = state.getGrafo().determinaDijkstra(idOrigen, idDestino, usarTiempo);

        if (!ruta.tieneRuta()) {
            boolean exito = false;
            resultadoTransferencia.setExito(exito);
            resultadoTransferencia.isExito();

            String mensaje = "No existe ruta entre " + origen.getNameSucursal() + " y " + destino.getNameSucursal();
            resultadoTransferencia.setMensaje(mensaje);
            resultadoTransferencia.getMensaje();

            return resultadoTransferencia;
        }

        resultadoTransferencia.setRuta(ruta.getRuta());
        origen.eliminarProducto(codigoProducto);
        productos.setStatus(Estado.TRANSITO);

        double etaAcumulado = 0;
        int[] rutaIds = ruta.getRuta();

        for (int i = 0; i < rutaIds.length; i++) {
            Sucursal sucursalActual = state.getCargaCSV().buscarSucursal(rutaIds[i]);

            if (sucursalActual == null) continue;

            boolean esOrigen = (i == 0);
            boolean esDestino = (i == rutaIds.length - 1);
            boolean esInter = (!esOrigen && !esDestino);

            sucursalActual.recibirProducto(productos);
            etaAcumulado += sucursalActual.getEntryTime();

            resultadoTransferencia.getPasos().add(new PasoTransferencia(rutaIds[i], sucursalActual.getNameSucursal(),
                    "INGRESO",
                    (long) (sucursalActual.getEntryTime() * 1000),
                    String.format("[%s] Ingreso procesado (+%ds) → %.0fs total", sucursalActual.getNameSucursal(),
                            sucursalActual.getEntryTime(),
                            etaAcumulado
                    )
            ));

            sucursalActual.getColaIngreso().dequeue();

            if (esDestino) {
                productos.setStatus(Estado.DISPONIBLE);


                productos.setBranchId(rutaIds[i]);

                boolean ok = sucursalActual.agregarProducto(productos);

                resultadoTransferencia.getPasos().add(new PasoTransferencia(
                        rutaIds[i],
                        sucursalActual.getNameSucursal(),
                        "DESTINO",
                        0,
                        String.format(
                                "🏁 [%s] Producto REGISTRADO en destino%s",
                                sucursalActual.getNameSucursal(),
                                ok ? " ✅" : " ⚠ (ya existía)"
                        )
                ));

            } else {

                sucursalActual.prepararTraspaso(productos);
                etaAcumulado += sucursalActual.getTransferTime();

                resultadoTransferencia.getPasos().add(new PasoTransferencia(
                        rutaIds[i],
                        sucursalActual.getNameSucursal(),
                        "TRASPASO",
                        (long) (sucursalActual.getTransferTime() * 1000),
                        String.format(
                                "[%s] Preparando traspaso (+%ds) → %.0fs",
                                sucursalActual.getNameSucursal(),
                                sucursalActual.getTransferTime(),
                                etaAcumulado
                        )
                ));

                sucursalActual.getColaTraspaso().dequeue();

                sucursalActual.alistarSalida(productos);
                etaAcumulado += sucursalActual.getDispatchInterval();

                resultadoTransferencia.getPasos().add(new PasoTransferencia(
                        rutaIds[i],
                        sucursalActual.getNameSucursal(),
                        "SALIDA",
                        (long) (sucursalActual.getDispatchInterval() * 1000),
                        String.format(
                                "[%s] Despachando (+%ds) → %.0fs",
                                sucursalActual.getNameSucursal(),
                                sucursalActual.getDispatchInterval(),
                                etaAcumulado
                        )
                ));

                sucursalActual.despachar();

                if (i + 1 < rutaIds.length) {
                    double pesoConexion = obtenerPeso(
                            rutaIds[i], rutaIds[i + 1], usarTiempo
                    );
                    etaAcumulado += pesoConexion;

                    resultadoTransferencia.getPasos().add(new PasoTransferencia(
                            rutaIds[i],
                            sucursalActual.getNameSucursal(),
                            "TRANSITO",
                            (long) (pesoConexion * 1000),
                            String.format(
                                    "🛣  En tránsito S%d → S%d (+%.0f%s) → %.0fs",
                                    rutaIds[i], rutaIds[i + 1],
                                    pesoConexion,
                                    usarTiempo ? "s" : "Q",
                                    etaAcumulado
                            )
                    ));
                }
            }
        }

        boolean exito = true;
        resultadoTransferencia.setExito(exito);
        resultadoTransferencia.isExito();

        resultadoTransferencia.setTotalSegundos(etaAcumulado);
        resultadoTransferencia.setMensaje(String.format(
                "✅ '%s' transferido de %s → %s en %.0f segundos (%.1f min)",
                productos.getName(),
                origen.getNameSucursal(),
                destino.getNameSucursal(),
                etaAcumulado,
                etaAcumulado / 60.0
        ));

        return resultadoTransferencia;
    }



    private double obtenerPeso(int origen, int destino,
                               boolean usarTiempo) {
        Grafo grafo = state.getGrafo();
        for (int i = 0; i < grafo.getTotalNodos(); i++) {
            if (grafo.getIdSucursal(i) == origen) {
                estructuras.grafos.NodoArista a =
                        grafo.getAristas(i);
                while (a != null) {
                    if (a.getDestino() == destino) {
                        return usarTiempo
                                ? a.getTiempo()
                                : a.getCosto();
                    }
                    a = a.getSiguiente();
                }
            }
        }
        return 0;
    }
}