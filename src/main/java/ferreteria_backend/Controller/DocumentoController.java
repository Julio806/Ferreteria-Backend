
package ferreteria_backend.Controller;

import ferreteria_backend.Entity.Documento;
import ferreteria_backend.Entity.Documento.TipoDocumento;
import ferreteria_backend.Service.DocumentoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(
            DocumentoService documentoService
    ) {
        this.documentoService = documentoService;
    }

    // ==========================================
    // SUBIR PLANTILLA
    // ==========================================

    @PostMapping(
            value = "/{tipo}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Documento> subirPlantilla(
            @PathVariable TipoDocumento tipo,
            @RequestParam("archivo") MultipartFile archivo
    ) {

        Documento documento =
                documentoService.subirPlantilla(tipo, archivo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documento);

    }

    // ==========================================
    // REEMPLAZAR PLANTILLA
    // ==========================================

    @PutMapping(
            value = "/{tipo}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Documento> reemplazarPlantilla(
            @PathVariable TipoDocumento tipo,
            @RequestParam("archivo") MultipartFile archivo
    ) {

        Documento documento =
                documentoService.subirPlantilla(tipo, archivo);

        return ResponseEntity.ok(documento);

    }

    // ==========================================
    // OBTENER TODAS LAS PLANTILLAS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Documento>> obtenerTodos() {

        return ResponseEntity.ok(
                documentoService.obtenerTodos()
        );

    }

    // ==========================================
    // OBTENER PLANTILLA POR TIPO
    // ==========================================

    @GetMapping("/{tipo}")
    public ResponseEntity<Documento> obtenerPorTipo(
            @PathVariable TipoDocumento tipo
    ) {

        return ResponseEntity.ok(
                documentoService.obtenerPorTipo(tipo)
        );

    }

    // ==========================================
    // ELIMINAR PLANTILLA
    // ==========================================

    @DeleteMapping("/{tipo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable TipoDocumento tipo
    ) {

        documentoService.eliminar(tipo);

        return ResponseEntity.noContent().build();

    }

}
