package ferreteria_backend.Service.impl;

import ferreteria_backend.Entity.Rol;
import ferreteria_backend.Exception.ResourceNotFoundException;
import ferreteria_backend.Repository.RolRepository;
import ferreteria_backend.Service.RolService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    // ==========================================
    // CREAR ROL
    // ==========================================

    @Override
    public Rol crear(Rol rol) {

        if (rol.getNombre() == null
                || rol.getNombre().isBlank()) {

            throw new RuntimeException(
                    "El nombre del rol es obligatorio"
            );
        }

        String nombre = rol.getNombre().trim();

        if (rolRepository.existsByNombre(nombre)) {

            throw new RuntimeException(
                    "Ya existe un rol con el nombre: "
                            + nombre
            );
        }

        rol.setNombre(nombre);

        return rolRepository.save(rol);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Override
    public Rol obtenerPorId(Long id) {

        return rolRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el rol con ID: "
                                        + id
                        )
                );
    }

    // ==========================================
    // OBTENER POR NOMBRE
    // ==========================================

    @Override
    public Rol obtenerPorNombre(String nombre) {

        return rolRepository.findByNombre(nombre)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el rol: "
                                        + nombre
                        )
                );
    }

    // ==========================================
    // OBTENER TODOS
    // ==========================================

    @Override
    public List<Rol> obtenerTodos() {

        return rolRepository.findAll();
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @Override
    public Rol actualizar(
            Long id,
            Rol rol
    ) {

        Rol rolExistente = obtenerPorId(id);

        if (rol.getNombre() == null
                || rol.getNombre().isBlank()) {

            throw new RuntimeException(
                    "El nombre del rol es obligatorio"
            );
        }

        String nuevoNombre =
                rol.getNombre().trim();

        if (!rolExistente.getNombre()
                .equalsIgnoreCase(nuevoNombre)
                && rolRepository.existsByNombre(nuevoNombre)) {

            throw new RuntimeException(
                    "Ya existe un rol con el nombre: "
                            + nuevoNombre
            );
        }

        rolExistente.setNombre(
                nuevoNombre
        );

        return rolRepository.save(
                rolExistente
        );
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @Override
    public void eliminar(Long id) {

        Rol rol = obtenerPorId(id);

        if (rol.getUsuarios() != null
                && !rol.getUsuarios().isEmpty()) {

            throw new RuntimeException(
                    "No se puede eliminar el rol porque "
                            + "tiene usuarios asociados"
            );
        }

        rolRepository.delete(rol);
    }
}