package ferreteria_backend.Repository;

import ferreteria_backend.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método original.
    // Lo utilizan otros servicios del sistema.
    Optional<Usuario> findByUsername(String username);

    // Método especial para autenticación.
    // Trae el usuario junto con su rol.
    @Query("""
            SELECT u
            FROM Usuario u
            JOIN FETCH u.rol
            WHERE u.username = :username
            """)
    Optional<Usuario> findByUsernameWithRol(
            @Param("username") String username
    );

    Optional<Usuario> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}