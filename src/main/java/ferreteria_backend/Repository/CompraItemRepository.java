
package ferreteria_backend.Repository;

import ferreteria_backend.Dto.Response.ProductoMovimientoProjection;
import ferreteria_backend.Entity.Compra;
import ferreteria_backend.Entity.CompraItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraItemRepository
        extends JpaRepository<CompraItem, Long> {

    @Query("""
        SELECT
            p.id AS productoId,
            p.nombre AS nombreProducto,
            SUM(ci.cantidad) AS unidades,
            SUM(ci.subtotal) AS importe
        FROM CompraItem ci
        JOIN ci.producto p
        JOIN ci.compra c
        WHERE c.estado = :estado
        AND c.fechaCreacion >= :inicio
        AND c.fechaCreacion < :fin
        GROUP BY p.id, p.nombre
        ORDER BY SUM(ci.cantidad) DESC
    """)
    List<ProductoMovimientoProjection> obtenerProductosMasComprados(
            @Param("estado") Compra.EstadoCompra estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}
