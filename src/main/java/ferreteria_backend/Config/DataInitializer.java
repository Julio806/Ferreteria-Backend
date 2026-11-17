package ferreteria_backend.Config;

import ferreteria_backend.Entity.Rol;
import ferreteria_backend.Entity.Usuario;
import ferreteria_backend.Repository.RolRepository;
import ferreteria_backend.Repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            // ==========================================
            // CREAR ROL ADMIN
            // ==========================================

            Rol rolAdmin = rolRepository
                    .findByNombre("ADMIN")
                    .orElseGet(() -> {

                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("ADMIN");

                        return rolRepository.save(nuevoRol);
                    });

            // ==========================================
            // CREAR USUARIO ADMIN
            // ==========================================

            if (!usuarioRepository.existsByUsername("admin")) {

                Usuario admin = new Usuario();

                admin.setNombre("Administrador");
                admin.setUsername("admin");
                admin.setEmail("admin@ferreteria.com");

                admin.setPassword(
                        passwordEncoder.encode("Admin1234")
                );

                admin.setActivo(true);
                admin.setRol(rolAdmin);

                usuarioRepository.save(admin);

                System.out.println(
                        "=========================================="
                );

                System.out.println(
                        "USUARIO ADMINISTRADOR CREADO"
                );

                System.out.println(
                        "Username: admin"
                );

                System.out.println(
                        "Password: Admin1234"
                );

                System.out.println(
                        "=========================================="
                );
            }
        };
    }
}
