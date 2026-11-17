package ferreteria_backend.Service.impl;

import ferreteria_backend.Dto.Response.DashboardResponse;
import ferreteria_backend.Dto.Response.PronosticoInventarioResponse;
import ferreteria_backend.Entity.Compra;
import ferreteria_backend.Entity.Producto;
import ferreteria_backend.Entity.Venta;
import ferreteria_backend.Entity.VentaItem;
import ferreteria_backend.Repository.CompraRepository;
import ferreteria_backend.Repository.ProductoRepository;
import ferreteria_backend.Repository.VentaItemRepository;
import ferreteria_backend.Repository.VentaRepository;
import ferreteria_backend.Service.DashboardService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int DIAS_HISTORIAL = 30;

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;
    private final VentaItemRepository ventaItemRepository;

    public DashboardServiceImpl(
            ProductoRepository productoRepository,
            VentaRepository ventaRepository,
            CompraRepository compraRepository,
            VentaItemRepository ventaItemRepository
    ) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.compraRepository = compraRepository;
        this.ventaItemRepository = ventaItemRepository;
    }

    // ==========================================
    // OBTENER INFORMACIÓN DEL DASHBOARD
    // ==========================================


    @Override
    public DashboardResponse obtenerDashboard() {

        String rol = obtenerRolActual();

        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.plusDays(1).atStartOfDay();

        // ==========================================
        // INFORMACIÓN GENERAL
        // ==========================================

        Long totalProductos = productoRepository.count();

        Long productosStockBajo = (long)
                productoRepository.findProductosConStockBajo().size();

        // ==========================================
        // VENTAS DEL DÍA
        // ==========================================

        List<Venta> listaVentasDelDia =
                ventaRepository.findByFechaCreacionBetween(
                        inicioDia,
                        finDia
                );

        BigDecimal totalVentasDelDia =
                calcularVentasDelDia(listaVentasDelDia);

        // ==========================================
        // RESPUESTA BASE
        // ==========================================

        DashboardResponse response = new DashboardResponse();

        response.setRol(rol);
        response.setTotalProductos(totalProductos);
        response.setProductosStockBajo(productosStockBajo);
        response.setVentasDelDia(totalVentasDelDia);
        response.setTotalVendidoDelDia(totalVentasDelDia);

        // ==========================================
        // PRONÓSTICO DE INVENTARIO
        // ADMIN Y VENDEDOR
        // ==========================================

        List<PronosticoInventarioResponse> pronosticos =
                generarPronosticoInventario();

        response.setPronosticosInventario(pronosticos);

        // ==========================================
        // DASHBOARD ADMINISTRADOR
        // ==========================================

        if ("ROLE_ADMIN".equals(rol)) {

            Long totalVentas = ventaRepository.countByEstado(
                    Venta.EstadoVenta.COMPLETADA
            );

            Long totalCompras = compraRepository.countByEstado(
                    Compra.EstadoCompra.COMPLETADA
            );

            List<Compra> listaComprasDelDia =
                    compraRepository.findByFechaCreacionBetween(
                            inicioDia,
                            finDia
                    );

            BigDecimal totalComprasDelDia =
                    calcularComprasDelDia(listaComprasDelDia);

            response.setTotalVentas(totalVentas);
            response.setTotalCompras(totalCompras);
            response.setComprasDelDia(totalComprasDelDia);
        }

        // ==========================================
        // DASHBOARD VENDEDOR
        // ==========================================

        if ("ROLE_VENDEDOR".equals(rol)) {

            response.setTotalVentas(null);
            response.setTotalCompras(null);
            response.setComprasDelDia(null);

            // No se elimina pronosticosInventario.
            // El vendedor también necesita consultar
            // el inventario y sus indicadores BI.
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

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElseThrow(() -> new RuntimeException(
                        "El usuario no tiene un rol asignado"
                ));
    }

    // ==========================================
    // CALCULAR VENTAS DEL DÍA
    // ==========================================

    private BigDecimal calcularVentasDelDia(List<Venta> ventas) {

        BigDecimal total = BigDecimal.ZERO;

        for (Venta venta : ventas) {

            if (venta.getEstado() == Venta.EstadoVenta.COMPLETADA
                    && venta.getTotal() != null) {

                total = total.add(venta.getTotal());
            }
        }

        return total;
    }

    // ==========================================
    // CALCULAR COMPRAS DEL DÍA
    // ==========================================

    private BigDecimal calcularComprasDelDia(List<Compra> compras) {

        BigDecimal total = BigDecimal.ZERO;

        for (Compra compra : compras) {

            if (compra.getEstado() == Compra.EstadoCompra.COMPLETADA
                    && compra.getTotal() != null) {

                total = total.add(compra.getTotal());
            }
        }

        return total;
    }

    // ==========================================
    // GENERAR PRONÓSTICO DE INVENTARIO
    // ==========================================

    private List<PronosticoInventarioResponse>
    generarPronosticoInventario() {

        LocalDateTime fechaFin = LocalDateTime.now();

        LocalDateTime fechaInicio =
                fechaFin.minusDays(DIAS_HISTORIAL);

        // Obtener detalles de ventas completadas
        // durante los últimos 30 días.

        List<VentaItem> movimientos =
                ventaItemRepository
                        .findByVenta_FechaCreacionBetweenAndVenta_Estado(
                                fechaInicio,
                                fechaFin,
                                Venta.EstadoVenta.COMPLETADA
                        );

        // Agrupar unidades vendidas por producto.

        Map<Long, Integer> unidadesVendidas = new HashMap<>();

        for (VentaItem item : movimientos) {

            if (item.getProducto() == null
                    || item.getCantidad() == null) {
                continue;
            }

            Long productoId = item.getProducto().getId();

            unidadesVendidas.merge(
                    productoId,
                    item.getCantidad(),
                    Integer::sum
            );
        }

        // Obtener productos activos.

        List<Producto> productos =
                productoRepository.findByActivoTrue();

        List<PronosticoInventarioResponse> resultado =
                new ArrayList<>();

        for (Producto producto : productos) {

            Integer vendidas = unidadesVendidas.getOrDefault(
                    producto.getId(),
                    0
            );

            int stockActual = producto.getStock() != null
                    ? producto.getStock()
                    : 0;

            int stockMinimo = producto.getStockMinimo() != null
                    ? producto.getStockMinimo()
                    : 0;

            // Promedio de unidades vendidas por día.

            BigDecimal promedioDiario = BigDecimal.valueOf(vendidas)
                    .divide(
                            BigDecimal.valueOf(DIAS_HISTORIAL),
                            2,
                            RoundingMode.HALF_UP
                    );

            Integer diasRestantes = null;
            LocalDate fechaProbableAgotamiento = null;

            // Calcular estimación solamente si existen ventas.

            if (promedioDiario.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal dias = BigDecimal.valueOf(stockActual)
                        .divide(
                                promedioDiario,
                                0,
                                RoundingMode.CEILING
                        );

                diasRestantes = dias.intValue();

                fechaProbableAgotamiento =
                        LocalDate.now().plusDays(diasRestantes);
            }

            // Generar recomendación.

            String recomendacion;

            if (stockActual <= 0) {

                recomendacion = "Producto agotado";

            } else if (stockActual <= stockMinimo) {

                recomendacion = "Reabastecimiento urgente";

            } else if (diasRestantes != null && diasRestantes <= 7) {

                recomendacion = "Reabastecer próximamente";

            } else if (diasRestantes != null && diasRestantes <= 15) {

                recomendacion = "Vigilar inventario";

            } else {

                recomendacion = "Inventario estable";
            }

            PronosticoInventarioResponse pronostico =
                    new PronosticoInventarioResponse();

            pronostico.setProductoId(producto.getId());
            pronostico.setCodigo(producto.getCodigo());
            pronostico.setNombre(producto.getNombre());
            pronostico.setStockActual(stockActual);
            pronostico.setStockMinimo(stockMinimo);
            pronostico.setUnidadesVendidasUltimos30Dias(vendidas);
            pronostico.setPromedioVentaDiaria(promedioDiario);
            pronostico.setDiasRestantes(diasRestantes);
            pronostico.setFechaProbableAgotamiento(
                    fechaProbableAgotamiento
            );
            pronostico.setRecomendacion(recomendacion);

            resultado.add(pronostico);
        }

        return resultado;
    }
}
