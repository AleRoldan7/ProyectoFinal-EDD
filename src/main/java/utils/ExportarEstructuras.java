package utils;

import clases.Productos;
import estructuras.arbolB.ArbolB;
import estructuras.arbolB.NodoB;
import estructuras.arbolBPlus.ArbolBPlus;
import estructuras.arbolBPlus.NodoBPlus;
import estructuras.avl.ArbolAVL;
import estructuras.avl.NodoAVL;
import estructuras.hash.TablaHash;
import estructuras.lista.ListaEnlazada;
import estructuras.nodo.Nodo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportarEstructuras {

    public static String avlToDot(ArbolAVL<Productos> avl, String nombreGrafo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(nombreGrafo).append(" {\n");
        sb.append("  graph [rankdir=TB, bgcolor=\"#fafafa\"];\n");
        sb.append("  node [shape=circle, style=filled, ").append("fontname=\"Arial\", fontsize=10];\n");
        sb.append("  edge [color=\"#bdc3c7\"];\n\n");

        if (!avl.isEmpty()) {
            avlNodoDot(sb, avl.getRoot(), null, true);
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static void avlNodoDot(StringBuilder sb, NodoAVL<Productos> nodo, NodoAVL<Productos> padre, boolean esRaiz) {
        if (nodo == null) return;

        String id = "n" + System.identityHashCode(nodo);
        String nom = nodo.producto != null ? nodo.producto.getName().replace("\"", "'") : "?";
        boolean esHoja = nodo.left == null && nodo.right == null;
        int fb = altN(nodo.left) - altN(nodo.right);

        String color = esRaiz ? "#e74c3c" : esHoja ? "#27ae60" : "#2980b9";

        sb.append(String.format("  %s [label=\"%s\\nh=%d FB=%d\", " + "fillcolor=\"%s\", fontcolor=\"white\"];\n", id, nom, nodo.altura, fb, color));

        if (padre != null) {
            String padreId = "n" + System.identityHashCode(padre);
            sb.append(String.format("  %s -> %s;\n", padreId, id));
        }

        avlNodoDot(sb, nodo.left, nodo, false);
        avlNodoDot(sb, nodo.right, nodo, false);
    }

    private static int altN(NodoAVL<Productos> n) {
        return (n == null) ? 0 : n.altura;
    }

    public static String arbolBToDot(ArbolB<String> arbol, String nombreGrafo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(nombreGrafo).append(" {\n");
        sb.append("  graph [rankdir=TB, bgcolor=\"#fafafa\"];\n");
        sb.append("  node [shape=record, style=filled, ").append("fontname=\"Arial\", fontsize=9];\n");
        sb.append("  edge [color=\"#bdc3c7\"];\n\n");

        if (!arbol.isEmpty()) {
            arbolBNodoDot(sb, arbol.getRaiz(), null, -1);
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static void arbolBNodoDot(StringBuilder sb, NodoB nodo, NodoB padre, int posHijo) {
        if (nodo == null) return;
        String id = "nb" + System.identityHashCode(nodo);

        // Construir etiqueta record con las claves
        StringBuilder etiq = new StringBuilder("{");
        for (int i = 0; i < nodo.getNumClaves(); i++) {
            if (i > 0) etiq.append("|");
            String clave = nodo.getClaves()[i] != null ? nodo.getClaves()[i].toString().replace("\"", "'") : "";
            etiq.append(clave);
        }
        etiq.append("}");

        String color = nodo.isEsHoja() ? "#27ae60" : "#8e44ad";

        sb.append(String.format("  %s [label=\"%s\", fillcolor=\"%s\", " + "fontcolor=\"white\"];\n", id, etiq, color));

        if (padre != null) {
            String padId = "nb" + System.identityHashCode(padre);
            sb.append(String.format("  %s -> %s;\n", padId, id));
        }

        if (!nodo.isEsHoja()) {
            for (int i = 0; i <= nodo.getNumClaves(); i++) {
                if (nodo.getHijos()[i] != null) arbolBNodoDot(sb, nodo.getHijos()[i], nodo, i);
            }
        }
    }

    public static String arbolBPlusToDot(ArbolBPlus<String> arbol, String nombreGrafo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(nombreGrafo).append(" {\n");
        sb.append("  graph [rankdir=TB, bgcolor=\"#1a1a2e\"];\n");
        sb.append("  node [shape=record, style=filled, ").append("fontname=\"Arial\", fontsize=9];\n");
        sb.append("  edge [color=\"#7f8c8d\"];\n\n");

        // Nodos del árbol
        if (!arbol.isEmpty()) {
            arbolBPlusNodoDot(sb, arbol.getRaiz(), null);
        }

        // Aristas de enlace entre hojas
        sb.append("\n  // enlaces entre hojas\n");
        sb.append("  edge [color=\"#f39c12\", ").append("style=dashed, constraint=false];\n");
        enlacesHojasDot(sb, arbol.getRaiz());

        sb.append("}\n");
        return sb.toString();
    }

    private static void arbolBPlusNodoDot(StringBuilder sb, NodoBPlus nodo, NodoBPlus padre) {
        if (nodo == null) return;
        String id = "nbp" + System.identityHashCode(nodo);

        StringBuilder etiq = new StringBuilder("{");
        for (int i = 0; i < nodo.getNumClaves(); i++) {
            if (i > 0) etiq.append("|");
            String c = nodo.getClaves()[i] != null ? nodo.getClaves()[i].toString().replace("\"", "'") : "";
            etiq.append(c);
        }
        etiq.append("}");

        String color = nodo.isEsHoja() ? "#d35400" : "#2c3e50";
        String fcolor = "white";

        sb.append(String.format("  %s [label=\"%s\", fillcolor=\"%s\", " + "fontcolor=\"%s\"];\n", id, etiq, color, fcolor));

        if (padre != null) {
            String padId = "nbp" + System.identityHashCode(padre);
            sb.append(String.format("  %s -> %s;\n", padId, id));
        }

        if (!nodo.isEsHoja()) {
            for (int i = 0; i <= nodo.getNumClaves(); i++) {
                if (nodo.getHijos()[i] != null) arbolBPlusNodoDot(sb, nodo.getHijos()[i], nodo);
            }
        }
    }

    private static void enlacesHojasDot(StringBuilder sb, NodoBPlus raiz) {
        NodoBPlus hoja = raiz;
        while (hoja != null && !hoja.isEsHoja()) {
            hoja = hoja.getHijos()[0];
        }

        while (hoja != null && hoja.getSiguiente() != null) {
            String id1 = "nbp" + System.identityHashCode(hoja);
            String id2 = "nbp" + System.identityHashCode(hoja.getSiguiente());
            sb.append(String.format("  %s -> %s;\n", id1, id2));
            hoja = hoja.getSiguiente();
        }
    }

    public static String hashToDot(TablaHash tabla, String nombreGrafo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(nombreGrafo).append(" {\n");
        sb.append("  graph [rankdir=LR, bgcolor=\"#fafafa\"];\n");
        sb.append("  node [shape=box, style=filled, ").append("fontname=\"Arial\", fontsize=9];\n\n");

        int cap = tabla.getCapacidad();
        for (int i = 0; i < cap; i++) {
            int tam = tabla.tamanioBucket(i);
            String idBucket = "b" + i;

            String color = tam == 0 ? "#ecf0f1" : tam == 1 ? "#27ae60" : "#e74c3c";
            String fcolor = tam == 0 ? "#95a5a6" : "white";

            sb.append(String.format("  %s [label=\"[%d]\\n%d elem\", " + "fillcolor=\"%s\", fontcolor=\"%s\"];\n", idBucket, i, tam, color, fcolor));

            if (tam > 1) {
                String prev = idBucket;
                for (int k = 1; k <= tam; k++) {
                    String idCol = "b" + i + "_" + k;
                    sb.append(String.format("  %s [label=\"col%d\", " + "fillcolor=\"#c0392b\", fontcolor=\"white\"];\n", idCol, k));
                    sb.append(String.format("  %s -> %s;\n", prev, idCol));
                    prev = idCol;
                }
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    public static String listaToDot(ListaEnlazada<Productos> lista, String nombreGrafo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(nombreGrafo).append(" {\n");
        sb.append("  graph [rankdir=LR, bgcolor=\"#fafafa\"];\n");
        sb.append("  node [shape=record, style=filled, ").append("fillcolor=\"#2980b9\", fontcolor=white, ").append("fontname=\"Arial\", fontsize=9];\n");
        sb.append("  edge [color=\"#e74c3c\"];\n\n");

        // Nodo NULL al final
        sb.append("  null [shape=plaintext, label=\"NULL\", ").append("fontcolor=\"#e74c3c\"];\n\n");

        Nodo<Productos> actual = lista.getHead();
        String prevId = null;
        int idx = 0;

        while (actual != null) {
            String id = "l" + idx;
            String nom = actual.producto != null ? actual.producto.getName().replace("\"", "'") : "?";
            String cod = actual.producto != null ? actual.producto.getBarCode() : "";

            sb.append(String.format("  %s [label=\"{%s|%s}\"];\n", id, nom, cod));

            if (prevId != null) {
                sb.append(String.format("  %s -> %s;\n", prevId, id));
            }

            prevId = id;
            actual = actual.next;
            idx++;
        }

        if (prevId != null) {
            sb.append(String.format("  %s -> null;\n", prevId));
        }

        sb.append("}\n");
        return sb.toString();
    }

    public static boolean guardarDot(String contenido, String rutaArchivo) {
        try (FileWriter fw = new FileWriter(rutaArchivo)) {
            fw.write(contenido);
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando .dot: " + e.getMessage());
            return false;
        }
    }

    public static boolean exportarAVLImagen(ArbolAVL<Productos> avl, String rutaArchivo, String formato) {
        if (avl == null || avl.isEmpty()) return false;

        int altura = avl.getHeight();
        int radio = Math.max(4, Math.min(18, 8000 / (int) Math.pow(2, altura)));
        int sepV = Math.max(30, radio * 3);

        int anchoTotal = (int) (Math.pow(2, altura) * (radio * 2 + 6));
        anchoTotal = Math.min(anchoTotal, 32000);
        int altoTotal = altura * sepV + 80;

        try {
            BufferedImage img = new BufferedImage(anchoTotal, altoTotal, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = img.createGraphics();

            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


            g2.setColor(java.awt.Color.decode("#fafafa"));
            g2.fillRect(0, 0, anchoTotal, altoTotal);

            g2.setColor(java.awt.Color.decode("#2c3e50"));
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            g2.drawString("Árbol AVL — " + avl.inOrden().size() + " nodos — Altura: " + altura, 10, 20);

            dibujarAVLSwing(g2, avl.getRoot(), anchoTotal / 2.0, 50, anchoTotal / 4.0, radio, sepV, true);

            g2.dispose();
            ImageIO.write(img, formato, new File(rutaArchivo));
            return true;

        } catch (Exception e) {
            System.err.println("Error exportando imagen: " + e.getMessage());
            return false;
        }
    }

    private static void dibujarAVLSwing(Graphics2D g2, NodoAVL<Productos> nodo, double x, double y, double offset, int radio, int sepV, boolean esRaiz) {
        if (nodo == null) return;

        boolean esHoja = nodo.left == null && nodo.right == null;
        int fb = altN(nodo.left) - altN(nodo.right);

        g2.setColor(java.awt.Color.decode("#bdc3c7"));
        g2.setStroke(new java.awt.BasicStroke(1.0f));
        if (nodo.left != null) {
            g2.drawLine((int) x, (int) y, (int) (x - offset), (int) (y + sepV));
            dibujarAVLSwing(g2, nodo.left, x - offset, y + sepV, offset / 2, radio, sepV, false);
        }
        if (nodo.right != null) {
            g2.drawLine((int) x, (int) y, (int) (x + offset), (int) (y + sepV));
            dibujarAVLSwing(g2, nodo.right, x + offset, y + sepV, offset / 2, radio, sepV, false);
        }

        java.awt.Color color = esRaiz ? java.awt.Color.decode("#e74c3c") : esHoja ? java.awt.Color.decode("#27ae60") : java.awt.Color.decode("#2980b9");

        g2.setColor(color);
        g2.fillOval((int) (x - radio), (int) (y - radio), radio * 2, radio * 2);
        g2.setColor(java.awt.Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawOval((int) (x - radio), (int) (y - radio), radio * 2, radio * 2);

        if (radio >= 8) {
            String nom = nodo.producto != null ? nodo.producto.getName() : "?";
            String txt = nom.length() > 4 ? nom.substring(0, 3) + "." : nom;
            g2.setColor(java.awt.Color.WHITE);
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, Math.max(6, radio / 2)));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(txt);
            g2.drawString(txt, (int) (x - tw / 2.0), (int) (y + fm.getAscent() / 2.0));
        }


        if (radio >= 10) {
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 7));
            g2.setColor(Math.abs(fb) <= 1 ? java.awt.Color.decode("#27ae60") : java.awt.Color.decode("#e74c3c"));
            g2.drawString("FB:" + fb, (int) (x - 10), (int) (y + radio + 12));
        }
    }

    public static boolean exportarHashImagen(TablaHash tabla, String rutaArchivo, String formato) {
        int cap = tabla.getCapacidad();
        int cols = 4;
        int bW = 200;
        int bH = 32;
        int gapX = 8;
        int gapY = 5;
        int padX = 15;
        int padY = 40;

        int filas = (int) Math.ceil((double) cap / cols);
        int imgW = cols * (bW + gapX) + padX * 2 + 200;
        int imgH = filas * (bH + gapY) + padY + 60;

        try {
            BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);


            g2.setColor(java.awt.Color.decode("#fafafa"));
            g2.fillRect(0, 0, imgW, imgH);


            g2.setColor(java.awt.Color.decode("#2c3e50"));
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
            g2.drawString(String.format("Tabla Hash — %d elementos / %d buckets — " + "Factor: %.3f", tabla.size(), cap, tabla.factorCarga()), padX, 25);

            int col = 0, fila = 0;

            for (int i = 0; i < cap; i++) {
                int tam = tabla.tamanioBucket(i);
                double bx = padX + col * (bW + gapX);
                double by = padY + fila * (bH + gapY);

                java.awt.Color color = tam == 0 ? java.awt.Color.decode("#ecf0f1") : tam == 1 ? java.awt.Color.decode("#27ae60") : java.awt.Color.decode("#e74c3c");

                g2.setColor(color);
                g2.fillRoundRect((int) bx, (int) by, bW, bH, 6, 6);
                g2.setColor(java.awt.Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(0.8f));
                g2.drawRoundRect((int) bx, (int) by, bW, bH, 6, 6);

                g2.setColor(tam == 0 ? java.awt.Color.GRAY : java.awt.Color.WHITE);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
                g2.drawString(String.format("[%d] %d elem%s", i, tam, tam > 1 ? " ⚠" : ""), (int) bx + 4, (int) by + bH / 2 + 4);


                if (tam > 1) {
                    for (int k = 1; k < Math.min(tam, 5); k++) {
                        int cx2 = (int) (bx + bW + 3 + (k - 1) * 34);
                        g2.setColor(java.awt.Color.decode("#c0392b"));
                        g2.fillRoundRect(cx2, (int) by, 30, bH, 4, 4);
                        g2.setColor(java.awt.Color.WHITE);
                        g2.drawString("→" + k, cx2 + 3, (int) by + bH / 2 + 4);
                    }
                }

                col++;
                if (col >= cols) {
                    col = 0;
                    fila++;
                }
            }


            int ly = padY + (fila + 1) * (bH + gapY) + 15;
            String[] labels = {"Sin colisión", "Con colisión", "Vacío"};
            java.awt.Color[] lColors = {java.awt.Color.decode("#27ae60"), java.awt.Color.decode("#e74c3c"), java.awt.Color.decode("#ecf0f1")};
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(lColors[i]);
                g2.fillRect(padX + i * 140, ly, 14, 14);
                g2.setColor(java.awt.Color.decode("#2c3e50"));
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
                g2.drawString(labels[i], padX + i * 140 + 18, ly + 11);
            }

            g2.dispose();
            ImageIO.write(img, formato, new File(rutaArchivo));
            return true;
        } catch (Exception e) {
            System.err.println("Error hash imagen: " + e.getMessage());
            return false;
        }
    }


    public static boolean exportarListaImagen(ListaEnlazada<Productos> lista, String rutaArchivo, String formato) {
        int total = lista.size();
        if (total == 0) return false;

        // Horizontal: hasta 20 por fila
        int porFila = 20;
        int nW = 160, nH = 50, gapX = 40, gapY = 30;
        int padX = 20, padY = 50;
        int filas = (int) Math.ceil((double) total / porFila);

        int imgW = Math.min(porFila, total) * (nW + gapX) + padX * 2;
        int imgH = filas * (nH + gapY) + padY + 30;

        try {
            BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(java.awt.Color.decode("#fafafa"));
            g2.fillRect(0, 0, imgW, imgH);

            g2.setColor(java.awt.Color.decode("#2c3e50"));
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
            g2.drawString("Lista Enlazada — " + total + " productos", padX, 25);

            Nodo<Productos> actual = lista.getHead();
            int idx = 0;
            int prevX = -1, prevY = -1;

            while (actual != null) {
                int fila = idx / porFila;
                int col = idx % porFila;
                int x = padX + col * (nW + gapX);
                int y = padY + fila * (nH + gapY);

                // Nodo
                g2.setColor(java.awt.Color.decode("#2980b9"));
                g2.fillRoundRect(x, y, nW, nH, 8, 8);
                g2.setColor(java.awt.Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(x, y, nW, nH, 8, 8);

                // Texto
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
                String nom = actual.producto != null ? actual.producto.getName() : "?";
                String cod = actual.producto != null ? actual.producto.getBarCode() : "";
                if (nom.length() > 18) nom = nom.substring(0, 17) + ".";

                g2.drawString(nom, x + 4, y + 18);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 8));
                g2.setColor(java.awt.Color.decode("#ecf0f1"));
                g2.drawString(cod, x + 4, y + 32);

                // Flecha al siguiente
                if (actual.next != null) {
                    int fila2 = (idx + 1) / porFila;
                    int col2 = (idx + 1) % porFila;
                    int x2 = padX + col2 * (nW + gapX);
                    int y2 = padY + fila2 * (nH + gapY);

                    g2.setColor(java.awt.Color.decode("#e74c3c"));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));

                    if (fila == fila2) {
                        // Misma fila: flecha horizontal
                        g2.drawLine(x + nW, y + nH / 2, x2, y2 + nH / 2);
                        // Punta
                        int ax = x2, ay = y2 + nH / 2;
                        int[] px = {ax, ax - 8, ax - 8};
                        int[] py = {ay, ay - 4, ay + 4};
                        g2.setColor(java.awt.Color.decode("#e74c3c"));
                        g2.fillPolygon(px, py, 3);
                    } else {
                        // Cambio de fila: flecha curva abajo
                        g2.drawLine(x + nW, y + nH / 2, x + nW + 15, y + nH / 2);
                        g2.drawLine(x + nW + 15, y + nH / 2, x2 - 15, y2 + nH / 2);
                        g2.drawLine(x2 - 15, y2 + nH / 2, x2, y2 + nH / 2);
                    }
                } else {
                    // NULL al final
                    g2.setColor(java.awt.Color.decode("#e74c3c"));
                    g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
                    g2.drawString("→ NULL", x + nW + 4, y + nH / 2 + 4);
                }

                actual = actual.next;
                idx++;
            }

            g2.dispose();
            ImageIO.write(img, formato, new File(rutaArchivo));
            return true;
        } catch (Exception e) {
            System.err.println("Error lista imagen: " + e.getMessage());
            return false;
        }
    }

    public static boolean exportarDotAImagen(String dot, String ruta, String formato) {
        try {
            // Archivo temporal .dot
            File tempDot = File.createTempFile("arbol", ".dot");
            FileWriter fw = new FileWriter(tempDot);
            fw.write(dot);
            fw.close();

            ProcessBuilder pb = new ProcessBuilder(
                    "dot", "-T" + formato,
                    tempDot.getAbsolutePath(),
                    "-o", ruta
            );

            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();

            tempDot.delete();
            return true;

        } catch (Exception e) {
            System.err.println("Error Graphviz: " + e.getMessage());
            return false;
        }
    }

}
