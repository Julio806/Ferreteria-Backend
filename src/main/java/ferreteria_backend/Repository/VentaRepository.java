
package ferreteria_backend.Repository;

import ferreteria_backend.Entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByFolio(String folio);

    List<Venta> findByUsuarioId(Long usuarioId);

    List<Venta> findByFechaCreacionBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    long countByEstado(Venta.EstadoVenta estado);

    // ==========================================
    // VENTAS PARA GENERAR REPORTES
    // ==========================================

    @Query("""
        SELECT DISTINCT v
        FROM Venta v
        JOIN FETCH v.usuario
        LEFT JOIN FETCH v.items item
        LEFT JOIN FETCH item.producto
        WHERE v.fechaCreacion >= :inicio
        AND v.fechaCreacion < :fin
        AND v.estado = :estado
        ORDER BY v.fechaCreacion ASC
    """)
    List<Venta> obtenerVentasParaReporte(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estado") Venta.EstadoVenta estado
    );

}
