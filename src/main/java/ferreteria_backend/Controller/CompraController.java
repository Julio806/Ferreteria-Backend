package ferreteria_backend.Controller;

import ferreteria_backend.Dto.Request.CompraRequest;
import ferreteria_backend.Dto.Response.CompraResponse;
import ferreteria_backend.Service.CompraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // ==========================================
    // CREAR COMPRA
    // ==========================================

    @PostMapping
    public ResponseEntity<CompraResponse> crear(
            @Valid @RequestBody CompraRequest request
    ) {

        CompraResponse nuevaCompra =
                compraService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaCompra);
    }

    // ==========================================
    // OBTENER TODAS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<CompraResponse>> obtenerTodas() {

        return ResponseEntity.ok(
                compraService.obtenerTodas()
        );
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                compraService.obtenerPorId(id)
        );
    }

    // ==========================================
    // COMPRAS DEL DÍA
    // ==========================================

    @GetMapping("/hoy")
    public ResponseEntity<List<CompraResponse>> obtenerComprasDelDia() {

        return ResponseEntity.ok(
                compraService.obtenerComprasDelDia()
        );
    }

    // ==========================================
    // COMPRAS POR FECHA
    // ==========================================

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<CompraResponse>> obtenerPorFecha(
            @PathVariable LocalDate fecha
    ) {

        return ResponseEntity.ok(
                compraService.obtenerPorFecha(fecha)
        );
    }

    // ==========================================
    // COMPRAS POR USUARIO
    // ==========================================

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CompraResponse>> obtenerPorUsuario(
            @PathVariable Long usuarioId
    ) {

        return ResponseEntity.ok(
                compraService.obtenerPorUsuario(usuarioId)
        );
    }

    // ==========================================
    // CANCELAR COMPRA
    // ==========================================

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id
    ) {

        compraService.cancelar(id);

        return ResponseEntity.noContent().build();
    }
}