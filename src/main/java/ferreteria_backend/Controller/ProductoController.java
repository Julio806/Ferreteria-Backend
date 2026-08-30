package ferreteria_backend.Controller;

import ferreteria_backend.Dto.Request.ProductoRequest;
import ferreteria_backend.Dto.Response.ProductoResponse;
import ferreteria_backend.Entity.Categoria;
import ferreteria_backend.Entity.Producto;
import ferreteria_backend.Service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ==========================================
    // CREAR PRODUCTO
    // ==========================================

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request
    ) {

        Producto producto = new Producto();

        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setMarca(request.getMarca());
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStock(request.getStock());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setUnidadMedida(request.getUnidadMedida());

        Categoria categoria = new Categoria();
        categoria.setId(request.getCategoriaId());

        producto.setCategoria(categoria);

        Producto guardado = productoService.crear(producto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(guardado));
    }

    // ==========================================
    // OBTENER TODOS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> obtenerTodos() {

        List<ProductoResponse> response =
                productoService.obtenerTodos()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // OBTENER ACTIVOS
    // ==========================================

    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponse>> obtenerActivos() {

        List<ProductoResponse> response =
                productoService.obtenerActivos()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(
            @PathVariable Long id
    ) {

        Producto producto =
                productoService.obtenerPorId(id);

        return ResponseEntity.ok(
                toResponse(producto)
        );
    }

    // ==========================================
    // OBTENER POR CÓDIGO
    // ==========================================

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoResponse> obtenerPorCodigo(
            @PathVariable String codigo
    ) {

        Producto producto =
                productoService.obtenerPorCodigo(codigo);

        return ResponseEntity.ok(
                toResponse(producto)
        );
    }

    // ==========================================
    // PRODUCTOS POR CATEGORÍA
    // ==========================================

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponse>> obtenerPorCategoria(
            @PathVariable Long categoriaId
    ) {

        List<ProductoResponse> response =
                productoService.obtenerPorCategoria(categoriaId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // BUSCAR POR NOMBRE
    // ==========================================

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombre(
            @RequestParam String nombre
    ) {

        List<ProductoResponse> response =
                productoService.buscarPorNombre(nombre)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // STOCK BAJO
    // ==========================================

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> obtenerStockBajo() {

        List<ProductoResponse> response =
                productoService.obtenerStockBajo()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request
    ) {

        Producto producto = new Producto();

        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setMarca(request.getMarca());
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStock(request.getStock());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setUnidadMedida(request.getUnidadMedida());

        Categoria categoria = new Categoria();
        categoria.setId(request.getCategoriaId());

        producto.setCategoria(categoria);

        Producto actualizado =
                productoService.actualizar(id, producto);

        return ResponseEntity.ok(
                toResponse(actualizado)
        );
    }

    // ==========================================
    // AUMENTAR STOCK
    // ==========================================

    @PatchMapping("/{id}/stock/aumentar")
    public ResponseEntity<ProductoResponse> aumentarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad
    ) {

        Producto producto =
                productoService.aumentarStock(id, cantidad);

        return ResponseEntity.ok(
                toResponse(producto)
        );
    }

    // ==========================================
    // DISMINUIR STOCK
    // ==========================================

    @PatchMapping("/{id}/stock/disminuir")
    public ResponseEntity<ProductoResponse> disminuirStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad
    ) {

        Producto producto =
                productoService.disminuirStock(id, cantidad);

        return ResponseEntity.ok(
                toResponse(producto)
        );
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // MAPPER
    // ==========================================

    private ProductoResponse toResponse(
            Producto producto
    ) {

        ProductoResponse response =
                new ProductoResponse();

        response.setId(producto.getId());
        response.setCodigo(producto.getCodigo());
        response.setNombre(producto.getNombre());
        response.setDescripcion(producto.getDescripcion());
        response.setMarca(producto.getMarca());
        response.setPrecioCompra(producto.getPrecioCompra());
        response.setPrecioVenta(producto.getPrecioVenta());
        response.setStock(producto.getStock());
        response.setStockMinimo(producto.getStockMinimo());
        response.setUnidadMedida(producto.getUnidadMedida());
        response.setActivo(producto.getActivo());

        if (producto.getCategoria() != null) {

            response.setCategoriaId(
                    producto.getCategoria().getId()
            );

            response.setCategoriaNombre(
                    producto.getCategoria().getNombre()
            );
        }

        return response;
    }
}