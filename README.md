# ProyectoFinal-EDD
# ProyectoFinal-EDD

Sistema de gestión y transferencia de inventario entre sucursales, implementado en **Java + JavaFX** con estructuras de datos propias (sin librerías externas de colecciones).

---

## Tabla de contenidos

1. [Descripción general](#descripción-general)
2. [Requisitos](#requisitos)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Compilación y ejecución](#compilación-y-ejecución)
5. [Archivos CSV de entrada](#archivos-csv-de-entrada)
6. [Estructuras de datos implementadas](#estructuras-de-datos-implementadas)
7. [Funcionalidades principales](#funcionalidades-principales)
8. [Concurrencia](#concurrencia)
9. [Exportación](#exportación)

---

## Descripción general

El sistema modela una red de sucursales conectadas por un grafo ponderado (tiempo y costo). Permite:

- Cargar sucursales, productos y conexiones desde archivos CSV.
- Transferir productos entre sucursales usando la ruta óptima (Dijkstra).
- Simular el flujo con colas de ingreso, traspaso y salida **en un hilo real**.
- Visualizar el grafo animado y exportarlo a `.dot`, PNG o JPG.
- Medir el rendimiento de búsquedas en AVL, Árbol B, B+, Hash y Lista.

---

## Requisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java JDK    | 17 o superior |
| JavaFX SDK  | 17 o superior |
| Maven       | 3.8+          |

> **Graphviz** (opcional): para convertir el `.dot` exportado a imagen desde la terminal.
> `dot -Tpng grafo_sucursales.dot -o grafo.png`

---

## Estructura del proyecto

```
ProyectoFinalEDD/
├── src/main/java/
│   ├── clases/
│   │   ├── Productos.java          — POJO de producto
│   │   └── Sucursal.java           — POJO de sucursal + sus colas
│   ├── estructuras/
│   │   ├── avl/                    — Árbol AVL genérico
│   │   ├── arbolB/                 — Árbol B de orden T
│   │   ├── arbolBPlus/             — Árbol B+ con enlaces entre hojas
│   │   ├── hash/                   — Tabla Hash con encadenamiento
│   │   ├── lista/                  — Lista enlazada simple genérica
│   │   ├── cola/                   — Cola FIFO basada en lista enlazada
│   │   ├── grafos/                 — Grafo de sucursales (lista de adyacencia)
│   │   └── nodo/                   — Nodo genérico reutilizable
│   ├── transferencia/
│   │   ├── TransfereciaProductos   — Dijkstra + lógica de transferencia
│   │   ├── TransferenciaTask.java  — Hilo real (Task<Void>)
│   │   ├── PasoTransferencia.java  — DTO de un paso del proceso
│   │   └── ResultadoTransferencia  — DTO resultado de Dijkstra
│   ├── ui/
│   │   ├── view/
│   │   │   ├── AppState.java       — Estado global compartido
│   │   │   ├── TransferenciaView   — Vista de transferencia con animación
│   │   │   └── RendimientoView     — Benchmarks de estructuras
│   │   └── estructuras_view/
│   │       ├── CargaCSVView        — Carga y validación de archivos
│   │       └── [otras vistas de estructuras]
│   └── utils/
│       └── ExportarEstructuras     — Exportación DOT / PNG / JPG
├── src/main/resources/
├── data/
│   ├── sucursales.csv
│   ├── productos.csv
│   └── conexiones.csv
├── pom.xml
└── README.md
```

---

## Compilación y ejecución

### Con Maven (recomendado)

```bash
# Compilar
mvn clean package -q

# Ejecutar
mvn javafx:run
```

### Sin Maven (manual con javac)

```bash
# Ajustar PATH_JAVAFX a tu instalación
export PATH_JAVAFX=/ruta/a/javafx-sdk-17/lib

javac --module-path $PATH_JAVAFX \
      --add-modules javafx.controls,javafx.fxml,javafx.swing \
      -cp src/main/java \
      -d out \
      $(find src/main/java -name "*.java")

java --module-path $PATH_JAVAFX \
     --add-modules javafx.controls,javafx.fxml,javafx.swing \
     -cp out \
     ui.MainApp
```

---

## Archivos CSV de entrada

Orden de carga obligatorio: **Sucursales → Productos → Conexiones**

### sucursales.csv

```
id,nombre,ciudad,zona
1,Central,Ciudad Capital,Zona 1
2,Norte,Ciudad Capital,Zona 18
...
```

### productos.csv

```
codigoBarras,nombre,categoria,precio,stock,idSucursal
IQU787Q1BJ,EcoGreen Pan 8958,Electronica,2257.03,77,8
...
```

- `codigoBarras`: exactamente **10 caracteres** alfanuméricos (A-Z, 0-9).
- `idSucursal`: debe coincidir con un ID ya cargado en `sucursales.csv`.
- Filas con errores se omiten y se registran en `errors.log`.

### conexiones.csv

```
origen,destino,tiempo,costo
1,2,20,15
2,1,20,15
...
```

- `tiempo`: minutos de traslado entre sucursales.
- `costo`: quetzales del traslado.
- El grafo es **no dirigido**: incluye ambas direcciones.

---

## Estructuras de datos implementadas

| Estructura       | Ubicación                  | Uso principal                              |
|------------------|----------------------------|--------------------------------------------|
| Lista Enlazada   | `estructuras/lista/`       | Inventario de cada sucursal                |
| Cola FIFO        | `estructuras/cola/`        | Colas de ingreso, traspaso y salida        |
| Árbol AVL        | `estructuras/avl/`         | Índice global de productos (búsqueda O(log n)) |
| Árbol B          | `estructuras/arbolB/`      | Índice por código de barras               |
| Árbol B+         | `estructuras/arbolBPlus/`  | Búsqueda por rango de precios             |
| Tabla Hash       | `estructuras/hash/`        | Búsqueda O(1) por código                  |
| Grafo (adj list) | `estructuras/grafos/`      | Red de sucursales, Dijkstra               |

Todas implementadas **desde cero** sin `java.util.List`, `java.util.Map`, ni ninguna colección de la JDK.

---

## Funcionalidades principales

### 1. Gestión de sucursales
- Cada sucursal tiene inventario independiente (lista enlazada propia).
- Identificadas por ID único, nombre, ciudad y zona.

### 2. Transferencia de productos
- Ruta óptima calculada con **Dijkstra** (criterio: tiempo mínimo o costo mínimo).
- Simulación paso a paso con animación visual del grafo.
- Procesamiento real en hilo dedicado (`TransferenciaTask`).

### 3. Búsqueda de productos
- Por código de barras: AVL, Árbol B, Hash.
- Por nombre / categoría: recorrido in-orden del AVL.
- Por rango de precio: Árbol B+.
- Por sucursal: lista enlazada de esa sucursal.

### 4. Medición de rendimiento
- Comparación de latencia (ns) entre las 5 estructuras para 1 000 búsquedas.
- Resultados mostrados en tabla y gráfico de barras.

### 5. Visualización
- Canvas JavaFX animado del grafo de sucursales.
- Vistas de árbol AVL, B, B+, lista y hash.

---

## Concurrencia

La transferencia de productos corre en un **Thread real** (`TransferenciaTask extends Task<Void>`):

- Se crea un `Thread` con `setDaemon(true)` para no bloquear el cierre de la app.
- Cada paso llama a `Thread.sleep(retardoMs)` para simular latencia real.
- Las actualizaciones de UI se hacen con `Platform.runLater(...)` (regla de JavaFX).
- El hilo puede interrumpirse con el botón **Reset** (`hiloTransferencia.interrupt()`).

```
UI Thread          TransferenciaTask (Thread real)
    │                       │
    │── new Thread(task) ──►│
    │                       │── ColaIngreso.encolar()
    │                       │── Thread.sleep(500ms)
    │◄── Platform.runLater ─│── ColaIngreso.desencolar()
    │  (actualizar tabla)   │── Thread.sleep(500ms)
    │◄── Platform.runLater ─│── ColaSalida.encolar()
    │  (dibujar nodo)       │── ...
```

---

## Exportación

Desde la vista de Transferencia:

| Formato | Herramienta           | Descripción                                      |
|---------|-----------------------|--------------------------------------------------|
| `.dot`  | `ExportarEstructuras` | Graphviz; resalta ruta activa en rojo            |
| `PNG`   | Java2D + ImageIO      | Captura del grafo con layout circular 1600×1000  |
| `JPG`   | Java2D + ImageIO      | Igual que PNG, fondo blanco (sin canal alfa)     |

Desde las demás vistas de estructuras (AVL, Hash, Lista) también se puede exportar PNG/JPG usando los métodos de `ExportarEstructuras`.

---

## Créditos

Proyecto desarrollado para el curso de **Estructuras de Datos** — Universidad San Carlos De Guatemala Centro Universitario De Occidente. 
Todas las estructuras de datos son implementaciones propias sin librerías externas de colecciones.
