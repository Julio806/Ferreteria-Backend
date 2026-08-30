package ferreteria_backend.Repository;

import ferreteria_backend.Entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository
        extends JpaRepository<Venta, Long> {

    boolean existsByFolio(String folio);

    List<Venta> findByUsuarioId(Long usuarioId);

    List<Venta> findByFechaCreacionBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    long countByEstado(Venta.EstadoVenta estado);
}