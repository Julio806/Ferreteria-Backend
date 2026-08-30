package ferreteria_backend.Service.impl;

import ferreteria_backend.Entity.Categoria;
import ferreteria_backend.Entity.Producto;
import ferreteria_backend.Exception.ResourceNotFoundException;
import ferreteria_backend.Repository.CategoriaRepository;
import ferreteria_backend.Repository.ProductoRepository;
import ferreteria_backend.Service.ProductoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

    @Override
    public Producto crear(Producto producto) {

        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new RuntimeException(
                    "Ya existe un producto con el código: "
                            + producto.getCodigo()
            );
        }

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

        if (producto.getStock() == null) {
            producto.setStock(0);
        }

        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(0);
        }

        producto.setActivo(true);

        return productoRepository.save(producto);
    }

    @Override
    public Producto obtenerPorId(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el producto con ID: " + id
                        )
                );

        return producto;
    }

    @Override
    public Producto obtenerPorCodigo(String codigo) {

        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el producto con código: "
                                        + codigo
                        )
                );
    }

    @Override
    public List<Producto> obtenerTodos() {

        return productoRepository.findAll();
    }

    @Override
    public List<Producto> obtenerActivos() {

        return productoRepository.findByActivoTrue();
    }

    @Override
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

    @Override
    public List<Producto> buscarPorNombre(String nombre) {

        return productoRepository
                .findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    @Override
    public List<Producto> obtenerStockBajo() {

        return productoRepository.findProductosConStockBajo();
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {

        Producto productoExistente = obtenerPorId(id);

        if (!productoExistente.getCodigo()
                .equalsIgnoreCase(producto.getCodigo())
                && productoRepository.existsByCodigo(producto.getCodigo())) {

            throw new RuntimeException(
                    "Ya existe un producto con el código: "
                            + producto.getCodigo()
            );
        }

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

        productoExistente.setCodigo(producto.getCodigo());
        productoExistente.setNombre(producto.getNombre());
        productoExistente.setDescripcion(producto.getDescripcion());
        productoExistente.setMarca(producto.getMarca());
        productoExistente.setPrecioCompra(producto.getPrecioCompra());
        productoExistente.setPrecioVenta(producto.getPrecioVenta());
        productoExistente.setStockMinimo(producto.getStockMinimo());
        productoExistente.setUnidadMedida(producto.getUnidadMedida());
        productoExistente.setCategoria(categoria);

        if (producto.getActivo() != null) {
            productoExistente.setActivo(producto.getActivo());
        }

        return productoRepository.save(productoExistente);
    }

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

    @Override
    public void eliminar(Long id) {

        Producto producto = obtenerPorId(id);

        producto.setActivo(false);

        productoRepository.save(producto);
    }
}