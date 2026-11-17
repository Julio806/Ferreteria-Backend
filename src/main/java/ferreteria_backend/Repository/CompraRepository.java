package ferreteria_backend.Repository;

import ferreteria_backend.Entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository
        extends JpaRepository<Compra, Long> {

    boolean existsByFolio(String folio);

    List<Compra> findByFechaCreacionBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Compra> findByUsuarioId(Long usuarioId);

    long countByEstado(Compra.EstadoCompra estado);
}
