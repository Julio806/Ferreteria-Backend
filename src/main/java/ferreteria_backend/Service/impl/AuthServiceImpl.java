package ferreteria_backend.Service.impl;

import ferreteria_backend.Dto.Request.LoginRequest;
import ferreteria_backend.Dto.Response.LoginResponse;
import ferreteria_backend.Entity.Rol;
import ferreteria_backend.Entity.Usuario;
import ferreteria_backend.Repository.UsuarioRepository;
import ferreteria_backend.Service.AuthService;
import ferreteria_backend.Security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario o contraseña incorrectos"
                        )
                );

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException(
                    "El usuario está desactivado"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword()
        )) {
            throw new RuntimeException(
                    "Usuario o contraseña incorrectos"
            );
        }

        Rol rol = usuario.getRol();

        if (rol == null) {
            throw new RuntimeException(
                    "El usuario no tiene un rol asignado"
            );
        }

        String token = jwtService.generarToken(
                usuario.getUsername(),
                rol.getNombre()
        );

        return new LoginResponse(
                usuario.getId(),
                token,
                usuario.getUsername(),
                rol.getNombre()
        );
    }
}