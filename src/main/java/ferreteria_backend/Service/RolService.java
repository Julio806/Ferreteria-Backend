package ferreteria_backend.Service;


import ferreteria_backend.Entity.Rol;

import java.util.List;

public interface RolService {

    Rol crear(Rol rol);

    Rol obtenerPorId(Long id);

    Rol obtenerPorNombre(String nombre);

    List<Rol> obtenerTodos();

    Rol actualizar(Long id, Rol rol);

    void eliminar(Long id);
}