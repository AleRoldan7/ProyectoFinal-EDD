package utils;

import clases.Productos;
import clases.Sucursal;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import estructuras.lista.ListaEnlazada;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CargaCSV {


    private static final String ERROR_LOG = "errors.log";

    // Lista propia de sucursales (sin HashMap)
    private Sucursal[] sucursales    = new Sucursal[200];
    private int        totalSucursal = 0;

    // ─────────────────────────────────────────
    // BUSCAR SUCURSAL POR ID
    // ─────────────────────────────────────────

    public Sucursal buscarSucursal(int id) {
        for (int i = 0; i < totalSucursal; i++) {
            if (sucursales[i].getIdSucursal() == id)
                return sucursales[i];
        }
        return null;
    }

    // ─────────────────────────────────────────
    // CARGAR SUCURSALES
    // Formato: ID,Nombre,Ubicacion,t_ingreso,t_traspaso,t_despacho
    // ─────────────────────────────────────────

    public void cargarSucursales(String archivo) {
        try (BufferedReader br =
                     new BufferedReader(new FileReader(archivo))) {

            String linea;
            br.readLine(); // saltar encabezado
            int numLinea = 1;

            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] p = linea.split(",");
                try {
                    if (p.length < 6) {
                        logError("Sucursales línea " + numLinea
                                + ": campos insuficientes");
                        continue;
                    }
                    int    id    = Integer.parseInt(p[0].trim());
                    String nom   = p[1].trim();
                    String loc   = p[2].trim();
                    int    tIng  = Integer.parseInt(p[3].trim());
                    int    tTra  = Integer.parseInt(p[4].trim());
                    int    tDes  = Integer.parseInt(p[5].trim());

                    sucursales[totalSucursal] =
                            new Sucursal(id, nom, loc, tIng, tTra, tDes);
                    totalSucursal++;

                } catch (NumberFormatException e) {
                    logError("Sucursales línea " + numLinea
                            + ": número inválido → " + linea);
                }
            }
            System.out.println("Sucursales: " + totalSucursal);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // CARGAR PRODUCTOS
    // Formato: SucursalID,Nombre,CodigoBarra,Categoria,
    //          FechaCaducidad,Marca,Precio,Stock
    // ─────────────────────────────────────────

    public void cargarProductos(String archivo) {
        // Para detectar duplicados sin HashSet
        String[] codigosVistos = new String[100000];
        int      totalCodigos  = 0;
        int      cargados      = 0;
        int      errores       = 0;

        try (BufferedReader br =
                     new BufferedReader(new FileReader(archivo))) {

            String linea;
            br.readLine(); // saltar encabezado
            int numLinea = 1;

            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] p = linea.split(",");
                try {
                    if (p.length < 8) {
                        logError("Productos línea " + numLinea
                                + ": campos insuficientes");
                        errores++;
                        continue;
                    }

                    int    idSuc     = Integer.parseInt(p[0].trim());
                    String nombre    = p[1].trim();
                    String codigo    = p[2].trim();
                    String categoria = p[3].trim();
                    String fecha     = p[4].trim();
                    String marca     = p[5].trim();
                    double precio    = Double.parseDouble(p[6].trim());
                    int    stock     = Integer.parseInt(p[7].trim());

                    // Verificar duplicado de código de barra
                    boolean duplicado = false;
                    for (int i = 0; i < totalCodigos; i++) {
                        if (codigosVistos[i].equals(codigo)) {
                            duplicado = true;
                            break;
                        }
                    }
                    if (duplicado) {
                        logError("Productos línea " + numLinea
                                + ": código duplicado → " + codigo);
                        errores++;
                        continue;
                    }
                    codigosVistos[totalCodigos] = codigo;
                    totalCodigos++;

                    // Buscar sucursal
                    Sucursal suc = buscarSucursal(idSuc);
                    if (suc == null) {
                        logError("Productos línea " + numLinea
                                + ": sucursal inexistente ID=" + idSuc);
                        errores++;
                        continue;
                    }

                    Productos prod = new Productos(
                            idSuc, nombre, codigo,
                            categoria, fecha, marca, precio, stock
                    );

                    boolean ok = suc.agregarProducto(prod);
                    if (ok) cargados++;
                    else {
                        logError("Productos línea " + numLinea
                                + ": fallo inserción → " + nombre);
                        errores++;
                    }

                } catch (NumberFormatException e) {
                    logError("Productos línea " + numLinea
                            + ": número inválido → " + linea);
                    errores++;
                }
            }
            System.out.println("Cargados: " + cargados
                    + " | Errores: " + errores);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // LOG DE ERRORES
    // ─────────────────────────────────────────

    private void logError(String mensaje) {
        try (FileWriter fw = new FileWriter(ERROR_LOG, true)) {
            fw.write("[" + new java.util.Date() + "] "
                    + mensaje + "\n");
        } catch (IOException e) {
            System.err.println("No se pudo escribir errors.log");
        }
    }

    // ─────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────

    public Sucursal[] getSucursales()  { return sucursales; }
    public int        getTotalSucursales() { return totalSucursal; }

    // Para la UI — retorna lista de sucursales como List
    public List<Sucursal> getListaSucursales() {
        List<Sucursal> lista = new ArrayList<>();
        for (int i = 0; i < totalSucursal; i++) {
            lista.add(sucursales[i]);
        }
        return lista;
    }
}

