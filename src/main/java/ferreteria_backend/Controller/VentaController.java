package ferreteria_backend.Controller;

import ferreteria_backend.Dto.Request.VentaRequest;
import ferreteria_backend.Dto.Response.VentaResponse;
import ferreteria_backend.Service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // ==========================================
    // CREAR VENTA
    // ==========================================

    @PostMapping
    public ResponseEntity<VentaResponse> crear(
            @Valid @RequestBody VentaRequest request
    ) {

        VentaResponse nuevaVenta =
                ventaService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaVenta);
    }

    // ==========================================
    // OBTENER TODAS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<VentaResponse>> obtenerTodas() {

        return ResponseEntity.ok(
                ventaService.obtenerTodas()
        );
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ventaService.obtenerPorId(id)
        );
    }

    // ==========================================
    // VENTAS DEL DÍA
    // ==========================================

    @GetMapping("/hoy")
    public ResponseEntity<List<VentaResponse>> obtenerVentasDelDia() {

        return ResponseEntity.ok(
                ventaService.obtenerVentasDelDia()
        );
    }

    // ==========================================
    // VENTAS POR FECHA
    // ==========================================

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<VentaResponse>> obtenerPorFecha(
            @PathVariable LocalDate fecha
    ) {

        return ResponseEntity.ok(
                ventaService.obtenerPorFecha(fecha)
        );
    }

    // ==========================================
    // VENTAS POR USUARIO
    // ==========================================

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VentaResponse>> obtenerPorUsuario(
            @PathVariable Long usuarioId
    ) {

        return ResponseEntity.ok(
                ventaService.obtenerPorUsuario(usuarioId)
        );
    }

    // ==========================================
    // CANCELAR VENTA
    // ==========================================

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id
    ) {

        ventaService.cancelar(id);

        return ResponseEntity.noContent().build();
    }
}