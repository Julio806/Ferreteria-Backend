package ferreteria_backend.Service;



import ferreteria_backend.Entity.Categoria;

import java.util.List;

public interface CategoriaService {

    Categoria crear(Categoria categoria);

    Categoria obtenerPorId(Long id);

    List<Categoria> obtenerTodas();

    List<Categoria> obtenerActivas();

    Categoria actualizar(Long id, Categoria categoria);

    void eliminar(Long id);
}
