package ferreteria_backend.Controller;


import ferreteria_backend.Dto.Request.CategoriaRequest;
import ferreteria_backend.Dto.Response.CategoriaResponse;
import ferreteria_backend.Entity.Categoria;
import ferreteria_backend.Service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//recibe peticiones
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // ==========================================
    // CREAR
    // ==========================================

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(
            @Valid @RequestBody CategoriaRequest request
    ) {

        Categoria categoria = new Categoria();

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria guardada = categoriaService.crear(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(guardada));
    }

    // ==========================================
    // OBTENER TODAS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> obtenerTodas() {

        List<CategoriaResponse> response =
                categoriaService.obtenerTodas()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // OBTENER ACTIVAS
    // ==========================================

    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponse>> obtenerActivas() {

        List<CategoriaResponse> response =
                categoriaService.obtenerActivas()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerPorId(
            @PathVariable Long id
    ) {

        System.out.println("=================================");
        System.out.println("BUSCANDO CATEGORIA ID: " + id);
        System.out.println("=================================");

        Categoria categoria =
                categoriaService.obtenerPorId(id);

        CategoriaResponse response = toResponse(categoria);

        System.out.println("CATEGORIA ENCONTRADA: "
                + response.getNombre());

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request
    ) {

        Categoria categoria = new Categoria();

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria actualizada =
                categoriaService.actualizar(
                        id,
                        categoria
                );

        return ResponseEntity.ok(
                toResponse(actualizada)
        );
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        categoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // MAPPER
    // ==========================================

    private CategoriaResponse toResponse(
            Categoria categoria
    ) {

        CategoriaResponse response =
                new CategoriaResponse();

        response.setId(categoria.getId());
        response.setNombre(categoria.getNombre());
        response.setDescripcion(categoria.getDescripcion());
        response.setActivo(categoria.getActivo());

        return response;
    }
}
