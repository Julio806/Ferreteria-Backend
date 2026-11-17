package ferreteria_backend.Repository;

import ferreteria_backend.Entity.Venta;
import ferreteria_backend.Entity.VentaItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaItemRepository
        extends JpaRepository<VentaItem, Long> {

    List<VentaItem>
    findByVenta_FechaCreacionBetweenAndVenta_Estado(
            LocalDateTime inicio,
            LocalDateTime fin,
            Venta.EstadoVenta estado
    );
}
