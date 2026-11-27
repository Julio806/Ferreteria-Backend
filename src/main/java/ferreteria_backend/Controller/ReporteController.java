
package ferreteria_backend.Controller;

import ferreteria_backend.Service.GeneradorReporteService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin
public class ReporteController {

    private final GeneradorReporteService generadorReporteService;

    public ReporteController(
            GeneradorReporteService generadorReporteService
    ) {
        this.generadorReporteService = generadorReporteService;
    }

    // =====================================================
    // VENTAS WORD
    // =====================================================

    @GetMapping("/ventas/word")
    public ResponseEntity<byte[]> generarVentasWord(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta

    ) {

        byte[] archivo = generadorReporteService.generarVentasWord(
                fechaDesde,
                fechaHasta
        );

        return generarRespuesta(
                archivo,
                "reporte-ventas.docx",
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
        );
    }

    // =====================================================
    // VENTAS EXCEL
    // =====================================================

    @GetMapping("/ventas/excel")
    public ResponseEntity<byte[]> generarVentasExcel(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta

    ) {

        byte[] archivo = generadorReporteService.generarVentasExcel(
                fechaDesde,
                fechaHasta
        );

        return generarRespuesta(
                archivo,
                "reporte-ventas.xlsx",
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );
    }

    // =====================================================
    // INVENTARIO WORD
    // =====================================================

    @GetMapping("/inventario/word")
    public ResponseEntity<byte[]> generarInventarioWord() {

        byte[] archivo =
                generadorReporteService.generarInventarioWord();

        return generarRespuesta(
                archivo,
                "reporte-inventario.docx",
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
        );
    }

    // =====================================================
    // INVENTARIO EXCEL
    // =====================================================

    @GetMapping("/inventario/excel")
    public ResponseEntity<byte[]> generarInventarioExcel() {

        byte[] archivo =
                generadorReporteService.generarInventarioExcel();

        return generarRespuesta(
                archivo,
                "reporte-inventario.xlsx",
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );
    }

    // =====================================================
    // RESPUESTA DE DESCARGA
    // =====================================================

    private ResponseEntity<byte[]> generarRespuesta(
            byte[] archivo,
            String nombreArchivo,
            MediaType tipoContenido
    ) {

        if (archivo == null || archivo.length == 0) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        }

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(tipoContenido);

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(nombreArchivo, StandardCharsets.UTF_8)
                        .build()
        );

        headers.setContentLength(archivo.length);

        return new ResponseEntity<>(
                archivo,
                headers,
                HttpStatus.OK
        );
    }
}
