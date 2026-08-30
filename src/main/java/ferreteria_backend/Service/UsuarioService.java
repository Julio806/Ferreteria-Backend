package ferreteria_backend.Service;

import ferreteria_backend.Entity.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario crear(Usuario usuario);

    Usuario obtenerPorId(Long id);

    Usuario obtenerPorUsername(String username);

    Usuario obtenerPorEmail(String email);

    List<Usuario> obtenerTodos();

    List<Usuario> obtenerActivos();

    Usuario actualizar(Long id, Usuario usuario);

    void cambiarEstado(Long id, Boolean activo);

    void eliminar(Long id);
}