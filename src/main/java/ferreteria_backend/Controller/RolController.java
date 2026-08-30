package ferreteria_backend.Controller;

import ferreteria_backend.Dto.Request.RolRequest;
import ferreteria_backend.Dto.Response.RolResponse;
import ferreteria_backend.Entity.Rol;
import ferreteria_backend.Service.RolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    // ==========================================
    // CREAR ROL
    // ==========================================

    @PostMapping
    public ResponseEntity<RolResponse> crear(
            @Valid @RequestBody RolRequest request
    ) {

        Rol rol = new Rol();

        rol.setNombre(request.getNombre());

        Rol nuevoRol = rolService.crear(rol);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertirAResponse(nuevoRol));
    }

    // ==========================================
    // OBTENER TODOS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<RolResponse>> obtenerTodos() {

        List<RolResponse> roles =
                rolService.obtenerTodos()
                        .stream()
                        .map(this::convertirAResponse)
                        .toList();

        return ResponseEntity.ok(roles);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> obtenerPorId(
            @PathVariable Long id
    ) {

        Rol rol = rolService.obtenerPorId(id);

        return ResponseEntity.ok(
                convertirAResponse(rol)
        );
    }

    // ==========================================
    // OBTENER POR NOMBRE
    // ==========================================

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<RolResponse> obtenerPorNombre(
            @PathVariable String nombre
    ) {

        Rol rol = rolService.obtenerPorNombre(nombre);

        return ResponseEntity.ok(
                convertirAResponse(rol)
        );
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RolRequest request
    ) {

        Rol rol = new Rol();

        rol.setNombre(request.getNombre());

        Rol actualizado =
                rolService.actualizar(id, rol);

        return ResponseEntity.ok(
                convertirAResponse(actualizado)
        );
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        rolService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // CONVERTIR ENTITY → RESPONSE
    // ==========================================

    private RolResponse convertirAResponse(Rol rol) {

        return new RolResponse(
                rol.getId(),
                rol.getNombre()
        );
    }
}