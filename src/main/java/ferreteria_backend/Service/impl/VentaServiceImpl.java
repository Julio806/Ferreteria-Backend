package ferreteria_backend.Service.impl;

import ferreteria_backend.Dto.Request.VentaItemRequest;
import ferreteria_backend.Dto.Request.VentaRequest;
import ferreteria_backend.Dto.Response.VentaItemResponse;
import ferreteria_backend.Dto.Response.VentaResponse;
import ferreteria_backend.Entity.Producto;
import ferreteria_backend.Entity.Usuario;
import ferreteria_backend.Entity.Venta;
import ferreteria_backend.Entity.VentaItem;
import ferreteria_backend.Repository.ProductoRepository;
import ferreteria_backend.Repository.UsuarioRepository;
import ferreteria_backend.Repository.VentaRepository;
import ferreteria_backend.Service.VentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final BigDecimal IMPUESTO_PORCENTAJE =
            new BigDecimal("0.16");

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // CREAR VENTA
    // ==========================================

    @Override
    public VentaResponse crear(VentaRequest request) {

        // ==========================================
        // VALIDAR USUARIO
        // ==========================================

        if (request.getUsuarioId() == null) {
            throw new RuntimeException(
                    "La venta debe tener un usuario"
            );
        }

        Usuario usuario = usuarioRepository.findById(
                request.getUsuarioId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "No se encontró el usuario con ID: "
                                + request.getUsuarioId()
                )
        );

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException(
                    "El usuario no está activo"
            );
        }

        // ==========================================
        // VALIDAR ITEMS
        // ==========================================

        if (request.getItems() == null
                || request.getItems().isEmpty()) {

            throw new RuntimeException(
                    "La venta debe contener al menos un producto"
            );
        }

        // ==========================================
        // CREAR VENTA
        // ==========================================

        Venta venta = new Venta();

        venta.setUsuario(usuario);

        venta.setFolio(generarFolio());

        venta.setMetodoPago(
                request.getMetodoPago()
        );

        venta.setEstado(
                Venta.EstadoVenta.COMPLETADA
        );

        List<VentaItem> items = new ArrayList<>();

        BigDecimal subtotalVenta = BigDecimal.ZERO;

        // ==========================================
        // PROCESAR PRODUCTOS
        // ==========================================

        for (VentaItemRequest itemRequest : request.getItems()) {

            if (itemRequest.getProductoId() == null) {
                throw new RuntimeException(
                        "Todos los productos deben tener un ID"
                );
            }

            if (itemRequest.getCantidad() == null
                    || itemRequest.getCantidad() <= 0) {

                throw new RuntimeException(
                        "La cantidad debe ser mayor que cero"
                );
            }

            // ==========================================
            // BUSCAR PRODUCTO
            // ==========================================

            Producto producto =
                    productoRepository.findById(
                            itemRequest.getProductoId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "No se encontró el producto con ID: "
                                            + itemRequest.getProductoId()
                            )
                    );

            // ==========================================
            // VALIDAR PRODUCTO ACTIVO
            // ==========================================

            if (!Boolean.TRUE.equals(producto.getActivo())) {

                throw new RuntimeException(
                        "El producto no está activo: "
                                + producto.getNombre()
                );
            }

            // ==========================================
            // VALIDAR STOCK
            // ==========================================

            if (producto.getStock()
                    < itemRequest.getCantidad()) {

                throw new RuntimeException(
                        "Stock insuficiente para el producto: "
                                + producto.getNombre()
                                + ". Stock disponible: "
                                + producto.getStock()
                );
            }

            // ==========================================
            // PRECIO ACTUAL
            // ==========================================

            BigDecimal precioUnitario =
                    producto.getPrecioVenta();

            // ==========================================
            // SUBTOTAL DEL ITEM
            // ==========================================

            BigDecimal subtotalItem =
                    precioUnitario.multiply(
                            BigDecimal.valueOf(
                                    itemRequest.getCantidad()
                            )
                    );

            // ==========================================
            // CREAR ITEM
            // ==========================================

            VentaItem item = new VentaItem();

            item.setVenta(venta);
            item.setProducto(producto);
            item.setCantidad(
                    itemRequest.getCantidad()
            );
            item.setPrecioUnitario(
                    precioUnitario
            );
            item.setSubtotal(
                    subtotalItem
            );

            items.add(item);

            // ==========================================
            // ACUMULAR SUBTOTAL
            // ==========================================

            subtotalVenta =
                    subtotalVenta.add(subtotalItem);

            // ==========================================
            // DESCONTAR STOCK
            // ==========================================

            producto.setStock(
                    producto.getStock()
                            - itemRequest.getCantidad()
            );

            productoRepository.save(producto);
        }

        // ==========================================
        // ASIGNAR ITEMS
        // ==========================================

        venta.setItems(items);

        // ==========================================
        // CALCULAR IMPUESTO
        // ==========================================

        BigDecimal impuesto =
                subtotalVenta.multiply(
                        IMPUESTO_PORCENTAJE
                );

        // ==========================================
        // CALCULAR TOTAL
        // ==========================================

        BigDecimal total =
                subtotalVenta.add(impuesto);

        venta.setSubtotal(subtotalVenta);
        venta.setImpuesto(impuesto);
        venta.setTotal(total);

        // ==========================================
        // GUARDAR
        // ==========================================

        Venta ventaGuardada =
                ventaRepository.save(venta);

        // ==========================================
        // CONVERTIR A RESPONSE
        // ==========================================

        return toResponse(ventaGuardada);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la venta con ID: "
                                        + id
                        )
                );

        return toResponse(venta);
    }

    // ==========================================
    // OBTENER TODAS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> obtenerTodas() {

        return ventaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ==========================================
    // OBTENER POR FECHA
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> obtenerPorFecha(
            LocalDate fecha
    ) {

        LocalDateTime inicio =
                fecha.atStartOfDay();

        LocalDateTime fin =
                fecha.plusDays(1)
                        .atStartOfDay();

        return ventaRepository
                .findByFechaCreacionBetween(
                        inicio,
                        fin
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ==========================================
    // OBTENER POR USUARIO
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> obtenerPorUsuario(
            Long usuarioId
    ) {

        if (!usuarioRepository.existsById(usuarioId)) {

            throw new RuntimeException(
                    "No se encontró el usuario con ID: "
                            + usuarioId
            );
        }

        return ventaRepository
                .findByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ==========================================
    // VENTAS DEL DÍA
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> obtenerVentasDelDia() {

        return obtenerPorFecha(
                LocalDate.now()
        );
    }

    // ==========================================
    // CANCELAR VENTA
    // ==========================================

    @Override
    public void cancelar(Long id) {

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la venta con ID: "
                                        + id
                        )
                );

        // ==========================================
        // VALIDAR ESTADO
        // ==========================================

        if (venta.getEstado()
                == Venta.EstadoVenta.CANCELADA) {

            throw new RuntimeException(
                    "La venta ya está cancelada"
            );
        }

        // ==========================================
        // DEVOLVER STOCK
        // ==========================================

        for (VentaItem item : venta.getItems()) {

            Producto producto =
                    productoRepository.findById(
                            item.getProducto().getId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "No se encontró el producto con ID: "
                                            + item.getProducto().getId()
                            )
                    );

            producto.setStock(
                    producto.getStock()
                            + item.getCantidad()
            );

            productoRepository.save(producto);
        }

        // ==========================================
        // CAMBIAR ESTADO
        // ==========================================

        venta.setEstado(
                Venta.EstadoVenta.CANCELADA
        );

        ventaRepository.save(venta);
    }

    // ==========================================
    // MAPPER VENTA → RESPONSE
    // ==========================================

    private VentaResponse toResponse(Venta venta) {

        VentaResponse response =
                new VentaResponse();

        response.setId(venta.getId());
        response.setFolio(venta.getFolio());

        response.setUsuarioId(
                venta.getUsuario().getId()
        );

        response.setUsuarioNombre(
                venta.getUsuario().getNombre()
        );

        response.setUsername(
                venta.getUsuario().getUsername()
        );

        response.setSubtotal(
                venta.getSubtotal()
        );

        response.setImpuesto(
                venta.getImpuesto()
        );

        response.setTotal(
                venta.getTotal()
        );

        response.setMetodoPago(
                venta.getMetodoPago()
        );

        response.setEstado(
                venta.getEstado()
        );

        response.setFechaCreacion(
                venta.getFechaCreacion()
        );

        List<VentaItemResponse> items =
                venta.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        response.setItems(items);

        return response;
    }

    // ==========================================
    // MAPPER ITEM → RESPONSE
    // ==========================================

    private VentaItemResponse toItemResponse(
            VentaItem item
    ) {

        VentaItemResponse response =
                new VentaItemResponse();

        response.setId(item.getId());

        response.setProductoId(
                item.getProducto().getId()
        );

        response.setProductoNombre(
                item.getProducto().getNombre()
        );

        response.setCantidad(
                item.getCantidad()
        );

        response.setPrecioUnitario(
                item.getPrecioUnitario()
        );

        response.setSubtotal(
                item.getSubtotal()
        );

        return response;
    }

    // ==========================================
    // GENERAR FOLIO
    // ==========================================

    private String generarFolio() {

        String folio;

        do {

            folio = "V-" +
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

        } while (
                ventaRepository.existsByFolio(folio)
        );

        return folio;
    }
}