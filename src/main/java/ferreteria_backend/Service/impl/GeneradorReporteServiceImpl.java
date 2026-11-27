
package ferreteria_backend.Service.impl;

import ferreteria_backend.Entity.Documento;
import ferreteria_backend.Entity.Documento.TipoDocumento;
import ferreteria_backend.Entity.Producto;
import ferreteria_backend.Entity.Venta;
import ferreteria_backend.Entity.VentaItem;

import ferreteria_backend.Repository.DocumentoRepository;
import ferreteria_backend.Repository.ProductoRepository;
import ferreteria_backend.Repository.VentaRepository;

import ferreteria_backend.Service.GeneradorReporteService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Consumer;

@Service
public class GeneradorReporteServiceImpl
        implements GeneradorReporteService {

    private final DocumentoRepository documentoRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GeneradorReporteServiceImpl(
            DocumentoRepository documentoRepository,
            ProductoRepository productoRepository,
            VentaRepository ventaRepository
    ) {
        this.documentoRepository = documentoRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    // =====================================================
    // GENERAR VENTAS WORD
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] generarVentasWord(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {

        validarFechas(fechaDesde, fechaHasta);

        List<Venta> ventas = obtenerVentas(
                fechaDesde,
                fechaHasta
        );

        return generarWord(
                TipoDocumento.VENTAS_WORD,
                documento -> rellenarVentasWord(documento, ventas)
        );
    }

    // =====================================================
    // GENERAR VENTAS EXCEL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] generarVentasExcel(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {

        validarFechas(fechaDesde, fechaHasta);

        List<Venta> ventas = obtenerVentas(
                fechaDesde,
                fechaHasta
        );

        return generarExcel(
                TipoDocumento.VENTAS_EXCEL,
                libro -> rellenarVentasExcel(libro, ventas)
        );
    }

    // =====================================================
    // GENERAR INVENTARIO WORD
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] generarInventarioWord() {

        List<Producto> productos =
                productoRepository.findByActivoTrue();

        return generarWord(
                TipoDocumento.INVENTARIO_WORD,
                documento -> rellenarInventarioWord(documento, productos)
        );
    }

    // =====================================================
    // GENERAR INVENTARIO EXCEL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] generarInventarioExcel() {

        List<Producto> productos =
                productoRepository.findByActivoTrue();

        return generarExcel(
                TipoDocumento.INVENTARIO_EXCEL,
                libro -> rellenarInventarioExcel(libro, productos)
        );
    }

    // =====================================================
    // OBTENER VENTAS POR PERIODO
    // =====================================================

    private List<Venta> obtenerVentas(
            LocalDate desde,
            LocalDate hasta
    ) {

        LocalDateTime inicio = desde.atStartOfDay();

        LocalDateTime finExclusivo =
                hasta.plusDays(1).atStartOfDay();

        return ventaRepository.obtenerVentasParaReporte(
                inicio,
                finExclusivo,
                Venta.EstadoVenta.COMPLETADA
        );
    }

    // =====================================================
    // VALIDAR FECHAS
    // =====================================================

    private void validarFechas(
            LocalDate desde,
            LocalDate hasta
    ) {

        if (desde == null || hasta == null) {
            throw new IllegalArgumentException(
                    "Debes indicar ambas fechas."
            );
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }
    }

    // =====================================================
    // OBTENER RUTA DE PLANTILLA
    // =====================================================

    private Path obtenerRutaPlantilla(TipoDocumento tipo) {

        Documento documento = documentoRepository
                .findByTipo(tipo)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se ha cargado la plantilla: " + tipo
                        )
                );

        Path ruta = Paths.get(documento.getRutaArchivo())
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(ruta) || !Files.isRegularFile(ruta)) {
            throw new IllegalStateException(
                    "No se encontró el archivo de plantilla: " + tipo
            );
        }

        return ruta;
    }


// =====================================================
// GENERAR WORD DIRECTAMENTE
// =====================================================

    private byte[] generarWord(
            TipoDocumento tipo,
            Consumer<XWPFDocument> rellenador
    ) {

        try {

            Path plantilla = obtenerRutaPlantilla(tipo);

            try (
                    InputStream entrada =
                            Files.newInputStream(plantilla);

                    XWPFDocument documento =
                            new XWPFDocument(entrada);

                    ByteArrayOutputStream salida =
                            new ByteArrayOutputStream()
            ) {

                // Diagnosticar la estructura de la plantilla.
                diagnosticarTablas(documento);

                // Rellenar la plantilla original.
                rellenador.accept(documento);

                // Generar directamente el archivo DOCX.
                documento.write(salida);

                byte[] resultado = salida.toByteArray();

                if (resultado.length == 0) {
                    throw new IllegalStateException(
                            "El documento Word generado está vacío."
                    );
                }

                return resultado;
            }

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "No se pudo generar el documento Word: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // GENERAR EXCEL
    // =====================================================

    private byte[] generarExcel(
            TipoDocumento tipo,
            Consumer<XSSFWorkbook> rellenador
    ) {

        try {

            Path plantilla = obtenerRutaPlantilla(tipo);

            try (
                    InputStream entrada =
                            Files.newInputStream(plantilla);

                    XSSFWorkbook libro =
                            new XSSFWorkbook(entrada);

                    ByteArrayOutputStream salida =
                            new ByteArrayOutputStream()
            ) {

                rellenador.accept(libro);

                libro.write(salida);

                return salida.toByteArray();
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo generar el archivo Excel: "
                            + e.getMessage(),
                    e
            );
        }
    }


// =====================================================
// RELLENAR WORD DE VENTAS
// =====================================================

    private void rellenarVentasWord(
            XWPFDocument documento,
            List<Venta> ventas
    ) {

        if (documento.getTables().size() < 2) {
            throw new IllegalStateException(
                    "La plantilla de ventas necesita dos tablas."
            );
        }

        XWPFTable tablaProductos = documento.getTables().get(0);
        XWPFTable tablaTotales = documento.getTables().get(1);

        if (tablaProductos.getNumberOfRows() < 2) {
            throw new IllegalStateException(
                    "No se encontró la fila modelo de productos."
            );
        }

        if (tablaTotales.getNumberOfRows() < 8) {
            throw new IllegalStateException(
                    "La tabla de totales no contiene las filas esperadas."
            );
        }

        List<VentaLinea> lineas = obtenerLineasVentas(ventas);

        System.out.println("========== REPORTE WORD ==========");
        System.out.println("Ventas: " + ventas.size());
        System.out.println("Productos: " + lineas.size());

        // Conservar únicamente el encabezado.
        while (tablaProductos.getNumberOfRows() > 1) {
            tablaProductos.removeRow(1);
        }

        // Crear directamente las filas de productos.
        for (VentaLinea linea : lineas) {

            XWPFTableRow fila = tablaProductos.createRow();

            String[] valores = {
                    linea.vendedor(),
                    linea.cantidad() == null
                            ? "0"
                            : String.valueOf(linea.cantidad()),
                    linea.nombre(),
                    moneda(linea.precio()),
                    moneda(linea.importe()),
                    linea.fecha()
            };

            for (int i = 0; i < valores.length; i++) {

                XWPFTableCell celda = fila.getCell(i);

                if (celda == null) {
                    throw new IllegalStateException(
                            "No se encontró la celda " + i
                    );
                }

                // Limpiar los párrafos anteriores.
                while (celda.getParagraphs().size() > 0) {
                    celda.removeParagraph(0);
                }

                // Escribir el contenido en un párrafo nuevo.
                XWPFParagraph parrafo = celda.addParagraph();
                XWPFRun run = parrafo.createRun();

                run.setFontFamily("Arial");
                run.setFontSize(9);
                run.setText(valores[i]);
            }

            System.out.println(
                    "Fila agregada: " + Arrays.toString(valores)
            );
        }

        // Mostrar mensaje cuando no existan ventas.
        if (lineas.isEmpty()) {
            XWPFTableRow fila = tablaProductos.createRow();
            XWPFTableCell celda = fila.getCell(0);

            if (celda != null) {
                celda.setText("Sin ventas en el periodo");
            }
        }

        // Calcular totales.
        BigDecimal subtotal = sumarVentas(
                ventas, Venta::getSubtotal
        );

        BigDecimal iva = sumarVentas(
                ventas, Venta::getImpuesto
        );

        BigDecimal total = sumarVentas(
                ventas, Venta::getTotal
        );

        // Actualizar tabla de totales.
        establecerCelda(tablaTotales.getRow(0), 1, moneda(subtotal));
        establecerCelda(tablaTotales.getRow(1), 1, "$0.00");
        establecerCelda(tablaTotales.getRow(6), 1, moneda(iva));
        establecerCelda(tablaTotales.getRow(7), 1, moneda(total));

        System.out.println(
                "Filas finales: " + tablaProductos.getNumberOfRows()
        );

        System.out.println("Subtotal: " + moneda(subtotal));
        System.out.println("IVA: " + moneda(iva));
        System.out.println("Total: " + moneda(total));
        System.out.println("==================================");
    }



    // =====================================================
    // RELLENAR WORD DE INVENTARIO
    // =====================================================


    // =====================================================
    // RELLENAR WORD DE INVENTARIO
    // =====================================================

    private void rellenarInventarioWord(
            XWPFDocument documento,
            List<Producto> productos
    ) {

        if (documento.getTables().isEmpty()) {
            throw new IllegalStateException(
                    "La plantilla de inventario no contiene una tabla."
            );
        }

        XWPFTable tabla = documento.getTables().get(0);

        if (tabla.getNumberOfRows() < 2) {
            throw new IllegalStateException(
                    "No se encontró la fila modelo del inventario."
            );
        }

        System.out.println("========== REPORTE INVENTARIO WORD ==========");
        System.out.println("Productos encontrados: " + productos.size());

        // Conservar únicamente el encabezado.
        while (tabla.getNumberOfRows() > 1) {
            tabla.removeRow(1);
        }

        // Crear una fila nueva por cada producto.
        for (Producto producto : productos) {

            XWPFTableRow fila = tabla.createRow();

            String[] valores = {
                    texto(producto.getCodigo()),
                    texto(producto.getNombre()),
                    texto(producto.getUnidadMedida()),
                    texto(producto.getMarca()),
                    moneda(producto.getPrecioCompra()),
                    String.valueOf(producto.getStock()),
                    formatearFecha(producto.getFechaCreacion()),
                    ""
            };

            // Escribir los ocho campos.
            for (int i = 0; i < valores.length; i++) {

                XWPFTableCell celda = fila.getCell(i);

                if (celda == null) {
                    throw new IllegalStateException(
                            "No se encontró la columna " + i
                    );
                }

                // Limpiar los párrafos anteriores.
                while (celda.getParagraphs().size() > 0) {
                    celda.removeParagraph(0);
                }

                // Crear un párrafo nuevo.
                XWPFParagraph parrafo = celda.addParagraph();

                XWPFRun run = parrafo.createRun();

                run.setFontFamily("Arial");
                run.setFontSize(8);
                run.setText(valores[i]);
            }

            System.out.println(
                    "Producto agregado: " + Arrays.toString(valores)
            );
        }

        // Mensaje cuando no existan productos.
        if (productos.isEmpty()) {

            XWPFTableRow fila = tabla.createRow();

            XWPFTableCell celda = fila.getCell(0);

            if (celda != null) {
                celda.setText("No hay productos registrados.");
            }
        }

        System.out.println(
                "Filas finales de inventario: " +
                        tabla.getNumberOfRows()
        );

        System.out.println("==============================================");
    }


    // =====================================================
    // RELLENAR EXCEL DE VENTAS
    // =====================================================

    private void rellenarVentasExcel(
            XSSFWorkbook libro,
            List<Venta> ventas
    ) {

        Sheet hoja = libro.getSheetAt(0);

        Row prototipo = hoja.getRow(10);

        if (prototipo == null) {
            throw new IllegalStateException(
                    "No se encontró la fila modelo de ventas Excel."
            );
        }

        CellStyle[] estilos =
                obtenerEstilos(prototipo, 8);

        short altura = prototipo.getHeight();

        Row filaCierre = hoja.getRow(11);

        if (filaCierre != null) {
            hoja.removeRow(filaCierre);
        }

        List<VentaLinea> lineas =
                obtenerLineasVentas(ventas);

        if (lineas.isEmpty()) {
            hoja.removeRow(prototipo);
        }

        for (int i = 0; i < lineas.size(); i++) {

            Row fila = i == 0
                    ? prototipo
                    : hoja.createRow(10 + i);

            fila.setHeight(altura);

            copiarEstilos(fila, estilos, 8);

            VentaLinea linea = lineas.get(i);

            establecerTextoExcel(fila, 2, linea.vendedor());
            establecerNumeroExcel(fila, 3, linea.cantidad());
            establecerTextoExcel(fila, 4, linea.nombre());
            establecerNumeroExcel(fila, 5, linea.precio());
            establecerNumeroExcel(fila, 6, linea.importe());
            establecerTextoExcel(fila, 7, linea.fecha());
        }

        int inicioTotales = 10 + lineas.size();

        BigDecimal subtotal = sumarVentas(
                ventas,
                Venta::getSubtotal
        );

        BigDecimal iva = sumarVentas(
                ventas,
                Venta::getImpuesto
        );

        BigDecimal total = sumarVentas(
                ventas,
                Venta::getTotal
        );

        agregarTotalExcel(
                hoja,
                inicioTotales,
                "Subtotal",
                subtotal
        );

        agregarTotalExcel(
                hoja,
                inicioTotales + 1,
                "IVA",
                iva
        );

        agregarTotalExcel(
                hoja,
                inicioTotales + 2,
                "TOTAL",
                total
        );

        agregarTextoExcel(
                hoja,
                inicioTotales + 3,
                "Total en letras",
                numeroALetras(total)
        );
    }

    // =====================================================
    // RELLENAR EXCEL DE INVENTARIO
    // =====================================================

    private void rellenarInventarioExcel(
            XSSFWorkbook libro,
            List<Producto> productos
    ) {

        Sheet hoja = libro.getSheetAt(0);

        Row prototipo = hoja.getRow(11);

        if (prototipo == null) {
            throw new IllegalStateException(
                    "No se encontró la fila modelo de inventario Excel."
            );
        }

        CellStyle[] estilos =
                obtenerEstilos(prototipo, 9);

        short altura = prototipo.getHeight();

        // Quitar celdas combinadas del prototipo.
        for (int i = hoja.getNumMergedRegions() - 1; i >= 0; i--) {

            CellRangeAddress rango =
                    hoja.getMergedRegion(i);

            if (rango.getFirstRow() >= 11 &&
                    rango.getLastRow() <= 12) {

                hoja.removeMergedRegion(i);
            }
        }

        // Eliminar fila de cierre.
        Row filaCierre = hoja.getRow(12);

        if (filaCierre != null) {
            hoja.removeRow(filaCierre);
        }

        if (productos.isEmpty()) {
            hoja.removeRow(prototipo);
        }

        for (int i = 0; i < productos.size(); i++) {

            Row fila = i == 0
                    ? prototipo
                    : hoja.createRow(11 + i);

            fila.setHeight(altura);

            copiarEstilos(fila, estilos, 9);

            Producto producto = productos.get(i);

            establecerTextoExcel(fila, 1, texto(producto.getCodigo()));
            establecerTextoExcel(fila, 2, texto(producto.getNombre()));
            establecerTextoExcel(fila, 3, texto(producto.getUnidadMedida()));
            establecerTextoExcel(fila, 4, texto(producto.getMarca()));

            establecerNumeroExcel(
                    fila,
                    5,
                    producto.getPrecioCompra()
            );

            establecerNumeroExcel(
                    fila,
                    6,
                    producto.getStock()
            );

            establecerTextoExcel(
                    fila,
                    7,
                    formatearFecha(producto.getFechaCreacion())
            );

            establecerTextoExcel(fila, 8, "");
        }
    }

    // =====================================================
    // OBTENER LÍNEAS DE VENTAS
    // =====================================================

    private List<VentaLinea> obtenerLineasVentas(
            List<Venta> ventas
    ) {

        List<VentaLinea> lineas = new ArrayList<>();

        for (Venta venta : ventas) {

            String vendedor = "";

            if (venta.getUsuario() != null) {

                vendedor = texto(
                        venta.getUsuario().getNombre()
                );

                if (vendedor.isBlank()) {
                    vendedor = texto(
                            venta.getUsuario().getUsername()
                    );
                }
            }

            if (venta.getItems() == null) {
                continue;
            }

            for (VentaItem item : venta.getItems()) {

                String nombreProducto = "";

                if (item.getProducto() != null) {
                    nombreProducto = texto(
                            item.getProducto().getNombre()
                    );
                }

                lineas.add(
                        new VentaLinea(
                                vendedor,
                                item.getCantidad(),
                                nombreProducto,
                                item.getPrecioUnitario(),
                                item.getSubtotal(),
                                formatearFecha(venta.getFechaCreacion())
                        )
                );
            }
        }

        return lineas;
    }

    // =====================================================
    // SUMAR IMPORTES
    // =====================================================

    private BigDecimal sumarVentas(
            List<Venta> ventas,
            Function<Venta, BigDecimal> extractor
    ) {

        return ventas.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // =====================================================
    // UTILIDADES WORD
    // =====================================================

    private void establecerCelda(
            XWPFTableRow fila,
            int indice,
            String valor
    ) {

        XWPFTableCell celda = fila.getCell(indice);

        if (celda == null) {
            throw new IllegalStateException(
                    "La fila de la plantilla no contiene la columna "
                            + indice
            );
        }

        if (celda.getParagraphs().isEmpty()) {
            celda.addParagraph();
        }

        XWPFParagraph parrafo =
                celda.getParagraphs().get(0);

        for (int i = celda.getParagraphs().size() - 1; i > 0; i--) {
            celda.removeParagraph(i);
        }

        establecerParrafo(parrafo, valor);
    }

    private void establecerParrafo(
            XWPFParagraph parrafo,
            String valor
    ) {

        List<XWPFRun> runs = parrafo.getRuns();

        if (runs.isEmpty()) {
            parrafo.createRun().setText(valor);
            return;
        }

        runs.get(0).setText(
                valor == null ? "" : valor,
                0
        );

        for (int i = runs.size() - 1; i > 0; i--) {
            parrafo.removeRun(i);
        }
    }

    // =====================================================
    // UTILIDADES EXCEL
    // =====================================================

    private CellStyle[] obtenerEstilos(
            Row fila,
            int columnas
    ) {

        CellStyle[] estilos = new CellStyle[columnas];

        for (int i = 0; i < columnas; i++) {

            Cell celda = fila.getCell(i);

            if (celda != null) {
                estilos[i] = celda.getCellStyle();
            }
        }

        return estilos;
    }

    private void copiarEstilos(
            Row fila,
            CellStyle[] estilos,
            int columnas
    ) {

        for (int i = 0; i < columnas; i++) {

            Cell celda = fila.getCell(i);

            if (celda == null) {
                celda = fila.createCell(i);
            }

            if (estilos[i] != null) {
                celda.setCellStyle(estilos[i]);
            }
        }
    }

    private void establecerTextoExcel(
            Row fila,
            int columna,
            String valor
    ) {

        Cell celda = fila.getCell(columna);

        if (celda == null) {
            celda = fila.createCell(columna);
        }

        celda.setCellValue(
                valor == null ? "" : valor
        );
    }

    private void establecerNumeroExcel(
            Row fila,
            int columna,
            Number valor
    ) {

        Cell celda = fila.getCell(columna);

        if (celda == null) {
            celda = fila.createCell(columna);
        }

        celda.setCellValue(
                valor == null ? 0 : valor.doubleValue()
        );
    }

    private void agregarTotalExcel(
            Sheet hoja,
            int filaIndice,
            String concepto,
            BigDecimal importe
    ) {

        Row fila = hoja.createRow(filaIndice);

        establecerTextoExcel(fila, 6, concepto);
        establecerNumeroExcel(fila, 7, importe);
    }

    private void agregarTextoExcel(
            Sheet hoja,
            int filaIndice,
            String concepto,
            String valor
    ) {

        Row fila = hoja.createRow(filaIndice);

        establecerTextoExcel(fila, 6, concepto);
        establecerTextoExcel(fila, 7, valor);
    }

    // =====================================================
    // FORMATO DE FECHAS Y VALORES
    // =====================================================

    private String formatearFecha(LocalDateTime fecha) {

        return fecha == null
                ? ""
                : fecha.format(FORMATO_FECHA);
    }

    private String moneda(BigDecimal valor) {

        return valor == null
                ? "$0.00"
                : "$" + valor.setScale(
                2,
                RoundingMode.HALF_UP
        ).toPlainString();
    }

    private String texto(String valor) {

        return valor == null ? "" : valor;
    }

    // =====================================================
    // CONVERTIR TOTAL A LETRAS
    // =====================================================

    private String numeroALetras(BigDecimal importe) {

        BigDecimal valor = importe.setScale(
                2,
                RoundingMode.HALF_UP
        );

        long entero = valor.longValue();

        int centavos = valor
                .remainder(BigDecimal.ONE)
                .movePointRight(2)
                .abs()
                .intValue();

        String letras = convertirNumero(entero);

        if (entero == 1) {
            letras = "UN PESO";
        } else {
            letras += " PESOS";
        }

        return letras + " "
                + String.format("%02d", centavos)
                + "/100 M.N.";
    }

    private String convertirNumero(long numero) {

        if (numero == 0) {
            return "CERO";
        }

        if (numero < 0 || numero > 999_999_999) {
            throw new IllegalArgumentException(
                    "Importe fuera del rango de conversión a letras."
            );
        }

        if (numero < 1000) {
            return convertirCentenas((int) numero);
        }

        if (numero < 1_000_000) {

            long miles = numero / 1000;
            long resto = numero % 1000;

            String textoMiles = miles == 1
                    ? "MIL"
                    : convertirNumero(miles) + " MIL";

            return resto == 0
                    ? textoMiles
                    : textoMiles + " " + convertirNumero(resto);
        }

        long millones = numero / 1_000_000;
        long resto = numero % 1_000_000;

        String textoMillones = millones == 1
                ? "UN MILLON"
                : convertirNumero(millones) + " MILLONES";

        return resto == 0
                ? textoMillones
                : textoMillones + " " + convertirNumero(resto);
    }

    private String convertirCentenas(int numero) {

        String[] unidades = {
                "", "UNO", "DOS", "TRES", "CUATRO",
                "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE",
                "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE",
                "QUINCE", "DIECISEIS", "DIECISIETE",
                "DIECIOCHO", "DIECINUEVE", "VEINTE",
                "VEINTIUNO", "VEINTIDOS", "VEINTITRES",
                "VEINTICUATRO", "VEINTICINCO", "VEINTISEIS",
                "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"
        };

        String[] decenas = {
                "", "", "", "TREINTA", "CUARENTA",
                "CINCUENTA", "SESENTA", "SETENTA",
                "OCHENTA", "NOVENTA"
        };

        String[] centenas = {
                "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS",
                "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS",
                "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
        };

        if (numero == 100) {
            return "CIEN";
        }

        if (numero < 30) {
            return unidades[numero];
        }

        if (numero < 100) {

            int decena = numero / 10;
            int unidad = numero % 10;

            return unidad == 0
                    ? decenas[decena]
                    : decenas[decena] + " Y " + unidades[unidad];
        }

        int centena = numero / 100;
        int resto = numero % 100;

        return resto == 0
                ? centenas[centena]
                : centenas[centena] + " " + convertirCentenas(resto);
    }

    // =====================================================
    // MODELO INTERNO DE PRODUCTO VENDIDO
    // =====================================================

    private record VentaLinea(
            String vendedor,
            Integer cantidad,
            String nombre,
            BigDecimal precio,
            BigDecimal importe,
            String fecha
    ) {
    }


// =====================================================
// DIAGNOSTICAR ESTRUCTURA DE TABLAS WORD
// =====================================================

    private void diagnosticarTablas(XWPFDocument documento) {

        System.out.println("\n");
        System.out.println("==============================================");
        System.out.println("       DIAGNÓSTICO DE PLANTILLA WORD");
        System.out.println("==============================================");

        System.out.println(
                "Total de tablas: " + documento.getTables().size()
        );

        for (int t = 0; t < documento.getTables().size(); t++) {

            XWPFTable tabla = documento.getTables().get(t);

            System.out.println("\n----------------------------------------------");
            System.out.println("TABLA: " + t);
            System.out.println("Número de filas: " + tabla.getNumberOfRows());

            for (int f = 0; f < tabla.getRows().size(); f++) {

                XWPFTableRow fila = tabla.getRow(f);

                System.out.println("\nFila: " + f);

                for (int c = 0; c < fila.getTableCells().size(); c++) {

                    String contenido = fila.getCell(c).getText();

                    System.out.println(
                            "Columna " + c + ": [" + contenido + "]"
                    );
                }
            }
        }

        System.out.println("\n==============================================");
        System.out.println("       FIN DEL DIAGNÓSTICO");
        System.out.println("==============================================\n");
    }

}
