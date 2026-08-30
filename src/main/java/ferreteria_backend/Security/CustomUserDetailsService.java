package ferreteria_backend.Security;

import ferreteria_backend.Entity.Rol;
import ferreteria_backend.Entity.Usuario;
import ferreteria_backend.Repository.UsuarioRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        Usuario usuario =
                usuarioRepository
                        .findByUsernameWithRol(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Usuario no encontrado: " + username
                                )
                        );

        Rol rol = usuario.getRol();

        if (rol == null) {
            throw new UsernameNotFoundException(
                    "El usuario no tiene un rol asignado"
            );
        }

        GrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + rol.getNombre()
                );

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(authority))
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}