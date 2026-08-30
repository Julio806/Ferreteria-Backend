package ferreteria_backend.Service.impl;

import ferreteria_backend.Entity.Rol;
import ferreteria_backend.Entity.Usuario;
import ferreteria_backend.Exception.ResourceNotFoundException;
import ferreteria_backend.Repository.RolRepository;
import ferreteria_backend.Repository.UsuarioRepository;
import ferreteria_backend.Service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // CREAR USUARIO
    // ==========================================

    @Override
    public Usuario crear(Usuario usuario) {

        // ==========================================
        // VALIDAR DATOS
        // ==========================================

        if (usuario.getNombre() == null
                || usuario.getNombre().isBlank()) {

            throw new RuntimeException(
                    "El nombre es obligatorio"
            );
        }

        if (usuario.getUsername() == null
                || usuario.getUsername().isBlank()) {

            throw new RuntimeException(
                    "El username es obligatorio"
            );
        }

        if (usuario.getEmail() == null
                || usuario.getEmail().isBlank()) {

            throw new RuntimeException(
                    "El email es obligatorio"
            );
        }

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            throw new RuntimeException(
                    "La contraseña es obligatoria"
            );
        }

        // ==========================================
        // VALIDAR USERNAME
        // ==========================================

        String username =
                usuario.getUsername().trim();

        if (usuarioRepository.existsByUsername(username)) {

            throw new RuntimeException(
                    "Ya existe un usuario con el username: "
                            + username
            );
        }

        // ==========================================
        // VALIDAR EMAIL
        // ==========================================

        String email =
                usuario.getEmail().trim();

        if (usuarioRepository.existsByEmail(email)) {

            throw new RuntimeException(
                    "Ya existe un usuario con el email: "
                            + email
            );
        }

        // ==========================================
        // VALIDAR ROL
        // ==========================================

        if (usuario.getRol() == null
                || usuario.getRol().getId() == null) {

            throw new RuntimeException(
                    "El usuario debe tener un rol"
            );
        }

        Long rolId =
                usuario.getRol().getId();

        Rol rol =
                rolRepository.findById(rolId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No se encontró el rol con ID: "
                                                + rolId
                                )
                        );

        // ==========================================
        // CONFIGURAR USUARIO
        // ==========================================

        usuario.setNombre(
                usuario.getNombre().trim()
        );

        usuario.setUsername(
                username
        );

        usuario.setEmail(
                email
        );

        usuario.setRol(
                rol
        );

        usuario.setActivo(
                true
        );

        // ==========================================
        // ENCRIPTAR PASSWORD
        // ==========================================

        usuario.setPassword(
                passwordEncoder.encode(
                        usuario.getPassword()
                )
        );

        return usuarioRepository.save(
                usuario
        );
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Override
    public Usuario obtenerPorId(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el usuario con ID: "
                                        + id
                        )
                );
    }

    // ==========================================
    // OBTENER POR USERNAME
    // ==========================================

    @Override
    public Usuario obtenerPorUsername(
            String username
    ) {

        return usuarioRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el usuario: "
                                        + username
                        )
                );
    }

    // ==========================================
    // OBTENER POR EMAIL
    // ==========================================

    @Override
    public Usuario obtenerPorEmail(
            String email
    ) {

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el usuario con email: "
                                        + email
                        )
                );
    }

    // ==========================================
    // OBTENER TODOS
    // ==========================================

    @Override
    public List<Usuario> obtenerTodos() {

        return usuarioRepository.findAll();
    }

    // ==========================================
    // OBTENER ACTIVOS
    // ==========================================

    @Override
    public List<Usuario> obtenerActivos() {

        return usuarioRepository.findAll()
                .stream()
                .filter(usuario ->
                        Boolean.TRUE.equals(
                                usuario.getActivo()
                        )
                )
                .toList();
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @Override
    public Usuario actualizar(
            Long id,
            Usuario usuario
    ) {

        Usuario usuarioExistente =
                obtenerPorId(id);

        // ==========================================
        // VALIDAR USERNAME
        // ==========================================

        if (usuario.getUsername() == null
                || usuario.getUsername().isBlank()) {

            throw new RuntimeException(
                    "El username es obligatorio"
            );
        }

        String username =
                usuario.getUsername().trim();

        if (!usuarioExistente.getUsername()
                .equalsIgnoreCase(username)
                && usuarioRepository
                .existsByUsername(username)) {

            throw new RuntimeException(
                    "Ya existe un usuario con el username: "
                            + username
            );
        }

        // ==========================================
        // VALIDAR EMAIL
        // ==========================================

        if (usuario.getEmail() == null
                || usuario.getEmail().isBlank()) {

            throw new RuntimeException(
                    "El email es obligatorio"
            );
        }

        String email =
                usuario.getEmail().trim();

        if (!usuarioExistente.getEmail()
                .equalsIgnoreCase(email)
                && usuarioRepository
                .existsByEmail(email)) {

            throw new RuntimeException(
                    "Ya existe un usuario con el email: "
                            + email
            );
        }

        // ==========================================
        // VALIDAR ROL
        // ==========================================

        if (usuario.getRol() == null
                || usuario.getRol().getId() == null) {

            throw new RuntimeException(
                    "El usuario debe tener un rol"
            );
        }

        Long rolId =
                usuario.getRol().getId();

        Rol rol =
                rolRepository.findById(rolId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No se encontró el rol con ID: "
                                                + rolId
                                )
                        );

        // ==========================================
        // ACTUALIZAR DATOS
        // ==========================================

        usuarioExistente.setNombre(
                usuario.getNombre().trim()
        );

        usuarioExistente.setUsername(
                username
        );

        usuarioExistente.setEmail(
                email
        );

        usuarioExistente.setRol(
                rol
        );

        if (usuario.getActivo() != null) {

            usuarioExistente.setActivo(
                    usuario.getActivo()
            );
        }

        // ==========================================
        // PASSWORD
        // ==========================================

        if (usuario.getPassword() != null
                && !usuario.getPassword().isBlank()) {

            usuarioExistente.setPassword(
                    passwordEncoder.encode(
                            usuario.getPassword()
                    )
            );
        }

        return usuarioRepository.save(
                usuarioExistente
        );
    }

    // ==========================================
    // CAMBIAR ESTADO
    // ==========================================

    @Override
    public void cambiarEstado(
            Long id,
            Boolean activo
    ) {

        Usuario usuario =
                obtenerPorId(id);

        usuario.setActivo(
                activo
        );

        usuarioRepository.save(
                usuario
        );
    }

    // ==========================================
    // ELIMINAR / DESACTIVAR
    // ==========================================

    @Override
    public void eliminar(Long id) {

        Usuario usuario =
                obtenerPorId(id);

        usuario.setActivo(
                false
        );

        usuarioRepository.save(
                usuario
        );
    }
}