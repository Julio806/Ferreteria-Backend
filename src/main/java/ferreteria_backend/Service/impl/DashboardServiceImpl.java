package ferreteria_backend.Service.impl;

import ferreteria_backend.Dto.Response.DashboardResponse;
import ferreteria_backend.Entity.Compra;
import ferreteria_backend.Entity.Venta;
import ferreteria_backend.Repository.CompraRepository;
import ferreteria_backend.Repository.ProductoRepository;
import ferreteria_backend.Repository.VentaRepository;
import ferreteria_backend.Service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;

    public DashboardServiceImpl(
            ProductoRepository productoRepository,
            VentaRepository ventaRepository,
            CompraRepository compraRepository
    ) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.compraRepository = compraRepository;
    }

    // ==========================================
    // OBTENER INFORMACIÓN DEL DASHBOARD
    // ==========================================

    @Override
    public DashboardResponse obtenerDashboard() {

        String rol = obtenerRolActual();

        // ==========================================
        // DATOS GENERALES
        // ==========================================

        Long totalProductos =
                productoRepository.count();

        Long productosStockBajo =
                (long) productoRepository
                        .findProductosConStockBajo()
                        .size();

        // ==========================================
        // FECHA ACTUAL
        // ==========================================

        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioDia =
                hoy.atStartOfDay();

        LocalDateTime finDia =
                hoy.plusDays(1)
                        .atStartOfDay();

        // ==========================================
        // VENTAS
        // ==========================================

        Long totalVentas =
                ventaRepository.countByEstado(
                        Venta.EstadoVenta.COMPLETADA
                );

        List<Venta> listaVentasDelDia =
                ventaRepository.findByFechaCreacionBetween(
                        inicioDia,
                        finDia
                );

        BigDecimal totalVentasDelDia =
                calcularVentasDelDia(
                        listaVentasDelDia
                );

        // ==========================================
        // RESPUESTA BASE
        // ==========================================

        DashboardResponse response =
                new DashboardResponse();

        response.setRol(rol);

        response.setTotalProductos(
                totalProductos
        );

        response.setProductosStockBajo(
                productosStockBajo
        );

        response.setVentasDelDia(
                totalVentasDelDia
        );

        response.setTotalVendidoDelDia(
                totalVentasDelDia
        );

        // ==========================================
        // DASHBOARD ADMINISTRADOR
        // ==========================================

        if ("ROLE_ADMIN".equals(rol)) {

            Long totalCompras =
                    compraRepository.countByEstado(
                            Compra.EstadoCompra.COMPLETADA
                    );

            List<Compra> listaComprasDelDia =
                    compraRepository
                            .findByFechaCreacionBetween(
                                    inicioDia,
                                    finDia
                            );

            BigDecimal totalComprasDelDia =
                    calcularComprasDelDia(
                            listaComprasDelDia
                    );

            response.setTotalVentas(
                    totalVentas
            );

            response.setTotalCompras(
                    totalCompras
            );

            response.setComprasDelDia(
                    totalComprasDelDia
            );
        }

        // ==========================================
        // DASHBOARD VENDEDOR
        // ==========================================

        if ("ROLE_VENDEDOR".equals(rol)) {

            /*
             * El vendedor no necesita recibir:
             *
             * - totalVentas
             * - totalCompras
             * - comprasDelDia
             *
             * Solamente recibe:
             *
             * - productos
             * - stock bajo
             * - ventas del día
             * - total vendido del día
             */

            response.setTotalVentas(null);
            response.setTotalCompras(null);
            response.setComprasDelDia(null);
        }

        return response;
    }

    // ==========================================
    // OBTENER ROL ACTUAL
    // ==========================================

    private String obtenerRolActual() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getAuthorities() == null) {

            throw new RuntimeException(
                    "No se pudo determinar el rol del usuario"
            );
        }

        return authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene un rol asignado"
                        )
                );
    }

    // ==========================================
    // CALCULAR VENTAS DEL DÍA
    // ==========================================

    private BigDecimal calcularVentasDelDia(
            List<Venta> ventas
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Venta venta : ventas) {

            if (venta.getEstado()
                    == Venta.EstadoVenta.COMPLETADA) {

                if (venta.getTotal() != null) {

                    total =
                            total.add(
                                    venta.getTotal()
                            );
                }
            }
        }

        return total;
    }

    // ==========================================
    // CALCULAR COMPRAS DEL DÍA
    // ==========================================

    private BigDecimal calcularComprasDelDia(
            List<Compra> compras
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Compra compra : compras) {

            if (compra.getEstado()
                    == Compra.EstadoCompra.COMPLETADA) {

                if (compra.getTotal() != null) {

                    total =
                            total.add(
                                    compra.getTotal()
                            );
                }
            }
        }

        return total;
    }
}