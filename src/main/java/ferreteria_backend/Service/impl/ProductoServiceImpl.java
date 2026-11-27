
package ferreteria_backend.Service.impl;

import ferreteria_backend.Entity.Categoria;
import ferreteria_backend.Entity.Producto;
import ferreteria_backend.Exception.ResourceNotFoundException;
import ferreteria_backend.Repository.CategoriaRepository;
import ferreteria_backend.Repository.ProductoRepository;
import ferreteria_backend.Service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // ==========================================
    // CREAR PRODUCTO
    // ==========================================

    @Override
    public Producto crear(Producto producto) {

        // Validar código interno duplicado
        if (productoRepository.existsByCodigo(producto.getCodigo())) {

            throw new RuntimeException(
                    "Ya existe un producto con el código: "
                            + producto.getCodigo()
            );
        }

        // Validar código de barras duplicado
        if (producto.getCodigoBarras() != null
                && !producto.getCodigoBarras().isBlank()
                && productoRepository.existsByCodigoBarras(
                producto.getCodigoBarras()
        )) {

            throw new RuntimeException(
                    "Ya existe un producto con el código de barras: "
                            + producto.getCodigoBarras()
            );
        }

        // Validar categoría
        if (producto.getCategoria() == null
                || producto.getCategoria().getId() == null) {

            throw new RuntimeException(
                    "El producto debe tener una categoría"
            );
        }

        Categoria categoria = categoriaRepository.findById(
                producto.getCategoria().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "No se encontró la categoría con ID: "
                                + producto.getCategoria().getId()
                )
        );

        producto.setCategoria(categoria);

        // Valores predeterminados
        if (producto.getStock() == null) {
            producto.setStock(0);
        }

        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(0);
        }

        producto.setActivo(true);

        return productoRepository.save(producto);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {

        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el producto con ID: " + id
                        )
                );
    }

    // ==========================================
    // OBTENER POR CÓDIGO
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorCodigo(String codigo) {

        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el producto con código: "
                                        + codigo
                        )
                );
    }

    // ==========================================
    // OBTENER POR CÓDIGO DE BARRAS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorCodigoBarras(String codigoBarras) {

        return productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el producto con código de barras: "
                                        + codigoBarras
                        )
                );
    }

    // ==========================================
    // OBTENER TODOS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {

        return productoRepository.findAll();
    }

    // ==========================================
    // OBTENER ACTIVOS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerActivos() {

        return productoRepository.findByActivoTrue();
    }

    // ==========================================
    // OBTENER POR CATEGORÍA
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorCategoria(Long categoriaId) {

        if (!categoriaRepository.existsById(categoriaId)) {

            throw new ResourceNotFoundException(
                    "No se encontró la categoría con ID: "
                            + categoriaId
            );
        }

        return productoRepository
                .findByCategoriaIdAndActivoTrue(categoriaId);
    }

    // ==========================================
    // BUSCAR POR NOMBRE
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {

        return productoRepository
                .findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    // ==========================================
    // OBTENER STOCK BAJO
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerStockBajo() {

        return productoRepository.findProductosConStockBajo();
    }

    // ==========================================
    // ACTUALIZAR PRODUCTO
    // ==========================================

    @Override
    public Producto actualizar(Long id, Producto producto) {

        Producto productoExistente = obtenerPorId(id);

        // Validar código interno duplicado
        if (!productoExistente.getCodigo()
                .equalsIgnoreCase(producto.getCodigo())
                && productoRepository.existsByCodigo(
                producto.getCodigo()
        )) {

            throw new RuntimeException(
                    "Ya existe un producto con el código: "
                            + producto.getCodigo()
            );
        }

        // Validar código de barras duplicado
        if (producto.getCodigoBarras() != null
                && !producto.getCodigoBarras().isBlank()) {

            boolean codigoBarrasDuplicado =
                    productoRepository.findByCodigoBarras(
                            producto.getCodigoBarras()
                    ).map(productoEncontrado ->
                            !productoEncontrado.getId().equals(id)
                    ).orElse(false);

            if (codigoBarrasDuplicado) {

                throw new RuntimeException(
                        "Ya existe otro producto con el código de barras: "
                                + producto.getCodigoBarras()
                );
            }
        }

        // Validar categoría
        if (producto.getCategoria() == null
                || producto.getCategoria().getId() == null) {

            throw new RuntimeException(
                    "El producto debe tener una categoría"
            );
        }

        Categoria categoria = categoriaRepository.findById(
                producto.getCategoria().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "No se encontró la categoría con ID: "
                                + producto.getCategoria().getId()
                )
        );

        // Actualizar datos del producto
        productoExistente.setCodigo(producto.getCodigo());

        productoExistente.setCodigoBarras(
                producto.getCodigoBarras()
        );

        productoExistente.setNombre(producto.getNombre());

        productoExistente.setDescripcion(
                producto.getDescripcion()
        );

        productoExistente.setMarca(producto.getMarca());

        productoExistente.setPrecioCompra(
                producto.getPrecioCompra()
        );

        productoExistente.setPrecioVenta(
                producto.getPrecioVenta()
        );

        productoExistente.setStockMinimo(
                producto.getStockMinimo()
        );

        productoExistente.setUnidadMedida(
                producto.getUnidadMedida()
        );

        productoExistente.setCategoria(categoria);

        if (producto.getActivo() != null) {
            productoExistente.setActivo(producto.getActivo());
        }

        return productoRepository.save(productoExistente);
    }

    // ==========================================
    // AUMENTAR STOCK
    // ==========================================

    @Override
    public Producto aumentarStock(Long id, Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {

            throw new RuntimeException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        Producto producto = obtenerPorId(id);

        producto.setStock(
                producto.getStock() + cantidad
        );

        return productoRepository.save(producto);
    }

    // ==========================================
    // DISMINUIR STOCK
    // ==========================================

    @Override
    public Producto disminuirStock(Long id, Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {

            throw new RuntimeException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        Producto producto = obtenerPorId(id);

        if (producto.getStock() < cantidad) {

            throw new RuntimeException(
                    "Stock insuficiente para el producto: "
                            + producto.getNombre()
            );
        }

        producto.setStock(
                producto.getStock() - cantidad
        );

        return productoRepository.save(producto);
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @Override
    public void eliminar(Long id) {

        Producto producto = obtenerPorId(id);

        producto.setActivo(false);

        productoRepository.save(producto);
    }
}
