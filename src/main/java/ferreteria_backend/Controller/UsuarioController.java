package ferreteria_backend.Controller;

import ferreteria_backend.Dto.Request.UsuarioRequest;
import ferreteria_backend.Dto.Response.RolResponse;
import ferreteria_backend.Dto.Response.UsuarioResponse;
import ferreteria_backend.Entity.Rol;
import ferreteria_backend.Entity.Usuario;
import ferreteria_backend.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // OBTENER TODOS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos() {

        List<UsuarioResponse> usuarios =
                usuarioService.obtenerTodos()
                        .stream()
                        .map(this::convertirAResponse)
                        .toList();

        return ResponseEntity.ok(usuarios);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(
            @PathVariable Long id
    ) {

        Usuario usuario = usuarioService.obtenerPorId(id);

        return ResponseEntity.ok(
                convertirAResponse(usuario)
        );
    }

    // ==========================================
    // OBTENER ACTIVOS
    // ==========================================

    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioResponse>> obtenerActivos() {

        List<UsuarioResponse> usuarios =
                usuarioService.obtenerActivos()
                        .stream()
                        .map(this::convertirAResponse)
                        .toList();

        return ResponseEntity.ok(usuarios);
    }

    // ==========================================
    // CREAR
    // ==========================================

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioRequest request
    ) {

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setActivo(
                request.getActivo() != null
                        ? request.getActivo()
                        : true
        );

        Rol rol = new Rol();
        rol.setId(request.getRolId());

        usuario.setRol(rol);

        Usuario creado = usuarioService.crear(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertirAResponse(creado));
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request
    ) {

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setActivo(request.getActivo());

        Rol rol = new Rol();
        rol.setId(request.getRolId());

        usuario.setRol(rol);

        Usuario actualizado =
                usuarioService.actualizar(id, usuario);

        return ResponseEntity.ok(
                convertirAResponse(actualizado)
        );
    }

    // ==========================================
    // CAMBIAR ESTADO
    // ==========================================

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo
    ) {

        usuarioService.cambiarEstado(id, activo);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        usuarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // CONVERTIR ENTITY → RESPONSE
    // ==========================================

    private UsuarioResponse convertirAResponse(
            Usuario usuario
    ) {

        RolResponse rolResponse = null;

        if (usuario.getRol() != null) {

            rolResponse = new RolResponse(
                    usuario.getRol().getId(),
                    usuario.getRol().getNombre()
            );
        }

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getActivo(),
                rolResponse
        );
    }
}