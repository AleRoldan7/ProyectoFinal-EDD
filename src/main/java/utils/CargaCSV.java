package utils;

import clases.Productos;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import estructuras.lista.ListaEnlazada;

import java.io.FileReader;
import java.io.IOException;

public class CargaCSV {


    String archivo = "/home/alejandro/Descargas/productos-cortos.csv";
    ListaEnlazada listaEnlazada = new ListaEnlazada();

    public void cargarArchivo() {
        try (CSVReader reader = new CSVReader(new FileReader(archivo))) {

            String[] linea;
            reader.readNext();

            while ((linea = reader.readNext()) != null) {
                try {

                    Productos productos = new Productos(
                            Integer.parseInt(linea[0].trim()),
                            linea[1].trim(),
                            linea[2].trim(),
                            linea[3].trim(),
                            linea[4].trim(),
                            linea[5].trim(),
                            Double.parseDouble(linea[6].trim()),
                            Integer.parseInt(linea[7].trim())
                    );
                    listaEnlazada.agregar(productos);


                } catch (Exception e) {
                    System.err.println("Error en la linea: " + String.join(",", linea));

                }
            }
            System.out.println("Si se agregaron los datos");
            System.out.println("Tamaño lista: " + listaEnlazada.size());
            listaEnlazada.mostrar();
        } catch(IOException | CsvValidationException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }


}


