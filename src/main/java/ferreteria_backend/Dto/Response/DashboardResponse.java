package ferreteria_backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // ==========================================
    // INFORMACIÓN GENERAL
    // ==========================================

    private Long totalProductos;

    private Long productosStockBajo;

    // ==========================================
    // VENTAS
    // ==========================================

    private Long totalVentas;

    private BigDecimal ventasDelDia;

    // ==========================================
    // COMPRAS
    // ==========================================

    private Long totalCompras;

    private BigDecimal comprasDelDia;

    // ==========================================
    // DATOS DEL VENDEDOR
    // ==========================================

    private BigDecimal totalVendidoDelDia;

    // ==========================================
    // ROL DEL USUARIO
    // ==========================================

    private String rol;
}