package ferreteria_backend.Service;


import ferreteria_backend.Entity.Producto;

import java.util.List;

public interface ProductoService {

    Producto crear(Producto producto);

    Producto obtenerPorId(Long id);

    Producto obtenerPorCodigo(String codigo);

    List<Producto> obtenerTodos();

    List<Producto> obtenerActivos();

    List<Producto> obtenerPorCategoria(Long categoriaId);

    List<Producto> buscarPorNombre(String nombre);

    List<Producto> obtenerStockBajo();

    Producto actualizar(Long id, Producto producto);

    Producto aumentarStock(Long id, Integer cantidad);

    Producto disminuirStock(Long id, Integer cantidad);

    void eliminar(Long id);
}