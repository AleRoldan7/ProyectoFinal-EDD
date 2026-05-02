package estructuras.grafos;

/**
 * ============================================================
 *  TEST MANUAL — Grafo + Dijkstra (tiempo vs. costo)
 *  Basado en el archivo conexiones.csv real del proyecto.
 * ============================================================
 *
 *  Red de prueba (10 sucursales, 14 conexiones bidireccionales):
 *
 *      Origen  Destino  Tiempo  Costo
 *      ──────  ───────  ──────  ─────
 *        1       2       240    150
 *        1       3        60     40
 *        1       5        90     60
 *        2       6       180    120
 *        2       4       300    200
 *        3       5       120     80
 *        4       7       360    250
 *        5       9       150    100
 *        6       7       420    300
 *        7      10       200    130
 *        8       9        90     55
 *        9      10       110     70
 *        1       8       200    140
 *        3       8       180    110
 *
 *  Diagrama simplificado:
 *
 *   [1]──60──[3]──120──[5]
 *    |    \        |
 *   200   180    150
 *    |      \      |
 *   [8]──90──[9]──110──[10]──200──[7]──360──[4]
 *
 *  Para ejecutar sin framework (solo JDK):
 *      javac estructuras/grafos/*.java
 *      java  estructuras.grafos.Test
 * ============================================================
 */

public class Test {

    // Colores ANSI para la salida en consola
    private static final String VERDE  = "\u001B[32m";
    private static final String ROJO   = "\u001B[31m";
    private static final String CYAN   = "\u001B[36m";
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";

    private static int pasados = 0;
    private static int fallidos = 0;

    // ─────────────────────────────────────────────────────────
    public static void main(String[] args) {

        Grafo grafo = construirGrafo();

        encabezado("GRUPO 1: Dijkstra por TIEMPO mínimo");
        // 1->10: 1->5->9->10 = 90+150+110 = 350
        testRuta(grafo, 1, 10, true,
                new int[]{1, 5, 9, 10}, 350.0,
                "1->10 por TIEMPO");

        // 1->7:  1->5->9->10->7 = 90+150+110+200 = 550
        testRuta(grafo, 1, 7, true,
                new int[]{1, 5, 9, 10, 7}, 550.0,
                "1->7 por TIEMPO");

        // 2->9:  2->1->5->9 = 240+90+150 = 480
        testRuta(grafo, 2, 9, true,
                new int[]{2, 1, 5, 9}, 480.0,
                "2->9 por TIEMPO");

        // 3->10: 3->5->9->10 = 120+150+110 = 380
        testRuta(grafo, 3, 10, true,
                new int[]{3, 5, 9, 10}, 380.0,
                "3->10 por TIEMPO");

        // 4->9:  4->7->10->9 = 360+200+110 = 670
        testRuta(grafo, 4, 9, true,
                new int[]{4, 7, 10, 9}, 670.0,
                "4->9 por TIEMPO");

        encabezado("GRUPO 2: Dijkstra por COSTO mínimo");
        // 1->10: 1->5->9->10 = 60+100+70 = 230
        testRuta(grafo, 1, 10, false,
                new int[]{1, 5, 9, 10}, 230.0,
                "1->10 por COSTO");

        // 1->7:  1->5->9->10->7 = 60+100+70+130 = 360
        testRuta(grafo, 1, 7, false,
                new int[]{1, 5, 9, 10, 7}, 360.0,
                "1->7 por COSTO");

        // 2->9:  2->1->5->9 = 150+60+100 = 310
        testRuta(grafo, 2, 9, false,
                new int[]{2, 1, 5, 9}, 310.0,
                "2->9 por COSTO");

        // *** CASO CLAVE: ruta diferente para tiempo vs costo ***
        // 3->10 COSTO: 3->8->9->10 = 110+55+70 = 235   (¡diferente a la de tiempo!)
        // 3->10 TIEMPO: 3->5->9->10 = 120+150+110 = 380
        testRuta(grafo, 3, 10, false,
                new int[]{3, 8, 9, 10}, 235.0,
                "3->10 por COSTO  *** ruta distinta que por TIEMPO ***");

        // 4->9:  4->7->10->9 = 250+130+70 = 450
        testRuta(grafo, 4, 9, false,
                new int[]{4, 7, 10, 9}, 450.0,
                "4->9 por COSTO");

        encabezado("GRUPO 3: Casos origen == destino");
        testRutaMismoNodo(grafo, 1, "origen==destino ID 1");
        testRutaMismoNodo(grafo, 7, "origen==destino ID 7");

        encabezado("GRUPO 4: Casos sin ruta (nodo inexistente)");
        testSinRuta(grafo, 1,  99, true,  "1->99 por TIEMPO (nodo 99 no existe)");
        testSinRuta(grafo, 99, 1,  false, "99->1  por COSTO (nodo 99 no existe)");
        testSinRuta(grafo, 50, 51, true,  "50->51 por TIEMPO (ambos inexistentes)");

        encabezado("GRUPO 5: Verificar bidireccionalidad");
        // Las rutas inversas deben tener el mismo peso total
        testPesoIgual(grafo, 1, 10, 10, 1, true,  "1->10 mismo peso que 10->1 (TIEMPO)");
        testPesoIgual(grafo, 3, 10, 10, 3, false, "3->10 mismo peso que 10->3 (COSTO)");

        encabezado("GRUPO 6: Nodo directo (arista directa)");
        // 1 y 3 están conectados directamente con tiempo=60, costo=40
        testRuta(grafo, 1, 3, true,
                new int[]{1, 3}, 60.0,
                "1->3 directo por TIEMPO");
        testRuta(grafo, 1, 3, false,
                new int[]{1, 3}, 40.0,
                "1->3 directo por COSTO");

        // ── Resumen final ──────────────────────────────────────
        System.out.println();
        System.out.println(BOLD + "══════════════════════════════════════════" + RESET);
        System.out.printf("%s  RESUMEN: %d/%d pruebas pasaron%s%n",
                fallidos == 0 ? VERDE : ROJO,
                pasados, pasados + fallidos,
                RESET);
        System.out.println(BOLD + "══════════════════════════════════════════" + RESET);
        if (fallidos > 0) {
            System.out.println(ROJO + "  Hay " + fallidos + " prueba(s) fallando." + RESET);
            System.exit(1);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Construcción del grafo con los datos del CSV real
    // ─────────────────────────────────────────────────────────
    private static Grafo construirGrafo() {
        Grafo g = new Grafo();
        //           origen  destino  tiempo   costo
        g.agregarConexionBiDireccional(1,  2,  240, 150.0);
        g.agregarConexionBiDireccional(1,  3,   60,  40.0);
        g.agregarConexionBiDireccional(1,  5,   90,  60.0);
        g.agregarConexionBiDireccional(2,  6,  180, 120.0);
        g.agregarConexionBiDireccional(2,  4,  300, 200.0);
        g.agregarConexionBiDireccional(3,  5,  120,  80.0);
        g.agregarConexionBiDireccional(4,  7,  360, 250.0);
        g.agregarConexionBiDireccional(5,  9,  150, 100.0);
        g.agregarConexionBiDireccional(6,  7,  420, 300.0);
        g.agregarConexionBiDireccional(7, 10,  200, 130.0);
        g.agregarConexionBiDireccional(8,  9,   90,  55.0);
        g.agregarConexionBiDireccional(9, 10,  110,  70.0);
        g.agregarConexionBiDireccional(1,  8,  200, 140.0);
        g.agregarConexionBiDireccional(3,  8,  180, 110.0);
        System.out.println(CYAN + "Grafo construido: " + g.getTotalNodos()
                + " sucursales, 14 conexiones bidireccionales" + RESET);
        System.out.println();
        return g;
    }

    // ─────────────────────────────────────────────────────────
    //  Aserciones
    // ─────────────────────────────────────────────────────────

    /** Verifica ruta exacta Y peso total */
    private static void testRuta(Grafo g, int origen, int destino,
                                 boolean usarTiempo,
                                 int[] rutaEsperada, double pesoEsperado,
                                 String nombre) {
        ResultadoRuta res = g.determinaDijkstra(origen, destino, usarTiempo);

        boolean tieneRuta = res.tieneRuta();
        boolean rutaOk    = tieneRuta && rutasIguales(res.getRuta(), rutaEsperada);
        boolean pesoOk    = tieneRuta && Math.abs(res.getPesoTotal() - pesoEsperado) < 0.01;

        boolean ok = tieneRuta && rutaOk && pesoOk;
        imprimir(ok, nombre,
                "Ruta=" + rutaToString(rutaEsperada) + " Peso=" + pesoEsperado,
                "Ruta=" + (tieneRuta ? rutaToString(res.getRuta()) : "NULL")
                        + " Peso=" + (tieneRuta ? res.getPesoTotal() : -1));
    }

    /** Caso origen == destino: debe devolver ruta de un solo nodo y peso 0 */
    private static void testRutaMismoNodo(Grafo g, int nodo, String nombre) {
        ResultadoRuta res = g.determinaDijkstra(nodo, nodo, true);
        // Según la implementación puede retornar ruta=[nodo] o ruta vacía con peso 0.
        // Aceptamos cualquiera que no sea "ruta inválida con peso > 0"
        boolean ok = !res.tieneRuta()
                || (res.getRuta().length == 1 && res.getRuta()[0] == nodo
                && res.getPesoTotal() == 0.0);
        imprimir(ok, nombre,
                "sin ruta o ruta=[" + nodo + "] con peso 0",
                res.tieneRuta()
                        ? "ruta=" + rutaToString(res.getRuta()) + " peso=" + res.getPesoTotal()
                        : "sin ruta (aceptable)"
        );
    }

    /** Nodo inexistente → tieneRuta() debe ser false */
    private static void testSinRuta(Grafo g, int origen, int destino,
                                    boolean usarTiempo, String nombre) {
        ResultadoRuta res = g.determinaDijkstra(origen, destino, usarTiempo);
        boolean ok = !res.tieneRuta();
        imprimir(ok, nombre, "tieneRuta()=false",
                "tieneRuta()=" + res.tieneRuta()
                        + (res.tieneRuta() ? " ruta=" + rutaToString(res.getRuta()) : ""));
    }

    /** Verifica que el peso A->B == peso B->A (grafo bidireccional simétrico) */
    private static void testPesoIgual(Grafo g,
                                      int o1, int d1,
                                      int o2, int d2,
                                      boolean usarTiempo, String nombre) {
        ResultadoRuta r1 = g.determinaDijkstra(o1, d1, usarTiempo);
        ResultadoRuta r2 = g.determinaDijkstra(o2, d2, usarTiempo);
        boolean ok = r1.tieneRuta() && r2.tieneRuta()
                && Math.abs(r1.getPesoTotal() - r2.getPesoTotal()) < 0.01;
        imprimir(ok, nombre,
                "peso(" + o1 + "->" + d1 + ") == peso(" + o2 + "->" + d2 + ")",
                r1.getPesoTotal() + " vs " + r2.getPesoTotal());
    }

    // ─────────────────────────────────────────────────────────
    //  Helpers de salida
    // ─────────────────────────────────────────────────────────
    private static void encabezado(String titulo) {
        System.out.println();
        System.out.println(BOLD + CYAN
                + "── " + titulo + " ──" + RESET);
    }

    private static void imprimir(boolean ok, String nombre,
                                 String esperado, String obtenido) {
        if (ok) {
            pasados++;
            System.out.printf("  %s[PASS]%s %s%n", VERDE, RESET, nombre);
            System.out.printf("         Ruta/Peso: %s%n", obtenido);
        } else {
            fallidos++;
            System.out.printf("  %s[FAIL]%s %s%n", ROJO, RESET, nombre);
            System.out.printf("         Esperado:  %s%n", esperado);
            System.out.printf("         Obtenido:  %s%n", obtenido);
        }
    }

    private static boolean rutasIguales(int[] a, int[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length)  return false;
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i]) return false;
        return true;
    }

    private static String rutaToString(int[] ruta) {
        if (ruta == null || ruta.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ruta.length; i++) {
            sb.append(ruta[i]);
            if (i < ruta.length - 1) sb.append("->");
        }
        return sb.append("]").toString();
    }
}
