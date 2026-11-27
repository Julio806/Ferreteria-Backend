package ferreteria_backend.Service.impl;

import ferreteria_backend.Dto.Request.CompraItemRequest;
import ferreteria_backend.Dto.Request.CompraRequest;
import ferreteria_backend.Dto.Response.CompraItemResponse;
import ferreteria_backend.Dto.Response.CompraResponse;
import ferreteria_backend.Entity.Compra;
import ferreteria_backend.Entity.CompraItem;
import ferreteria_backend.Entity.Producto;
import ferreteria_backend.Entity.Usuario;
import ferreteria_backend.Repository.CompraRepository;
import ferreteria_backend.Repository.ProductoRepository;
import ferreteria_backend.Repository.UsuarioRepository;
import ferreteria_backend.Service.CompraService;
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
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final BigDecimal IMPUESTO_PORCENTAJE =
            new BigDecimal("0.16");

    public CompraServiceImpl(
            CompraRepository compraRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.compraRepository = compraRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // CREAR COMPRA
    // ==========================================

    @Override
    public CompraResponse crear(CompraRequest request) {

        // ==========================================
        // VALIDAR USUARIO
        // ==========================================

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
                    "La compra debe contener al menos un producto"
            );
        }

        // ==========================================
        // CREAR COMPRA
        // ==========================================

        Compra compra = new Compra();

        compra.setUsuario(usuario);

        compra.setFolio(generarFolio());

        compra.setEstado(
                Compra.EstadoCompra.COMPLETADA
        );

        compra.setItems(new ArrayList<>());

        BigDecimal subtotalCompra = BigDecimal.ZERO;

        // ==========================================
        // PROCESAR PRODUCTOS
        // ==========================================

        for (CompraItemRequest itemRequest : request.getItems()) {

            // ==========================================
            // VALIDAR PRODUCTO
            // ==========================================

            if (itemRequest.getProductoId() == null) {

                throw new RuntimeException(
                        "El producto es obligatorio"
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
            // OBTENER PRECIO DE COMPRA
            // ==========================================

            BigDecimal precioUnitario =
                    producto.getPrecioCompra();

            // ==========================================
            // CALCULAR SUBTOTAL DEL ITEM
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

            CompraItem item = new CompraItem();

            item.setCompra(compra);
            item.setProducto(producto);
            item.setCantidad(itemRequest.getCantidad());
            item.setPrecioUnitario(precioUnitario);
            item.setSubtotal(subtotalItem);

            compra.getItems().add(item);

            // ==========================================
            // ACUMULAR SUBTOTAL
            // ==========================================

            subtotalCompra =
                    subtotalCompra.add(subtotalItem);

            // ==========================================
            // AUMENTAR STOCK
            // ==========================================

            producto.setStock(
                    producto.getStock()
                            + itemRequest.getCantidad()
            );

            productoRepository.save(producto);
        }

        // ==========================================
        // CALCULAR IMPUESTO
        // ==========================================

        BigDecimal impuesto =
                subtotalCompra.multiply(
                        IMPUESTO_PORCENTAJE
                );

        // ==========================================
        // CALCULAR TOTAL
        // ==========================================

        BigDecimal total =
                subtotalCompra.add(impuesto);

        compra.setSubtotal(subtotalCompra);
        compra.setImpuesto(impuesto);
        compra.setTotal(total);

        // ==========================================
        // GUARDAR COMPRA
        // ==========================================

        Compra compraGuardada =
                compraRepository.save(compra);

        return convertirAResponse(compraGuardada);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public CompraResponse obtenerPorId(Long id) {

        Compra compra =
                compraRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No se encontró la compra con ID: "
                                                + id
                                )
                        );

        return convertirAResponse(compra);
    }

    // ==========================================
    // OBTENER TODAS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponse> obtenerTodas() {

        return compraRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // ==========================================
    // OBTENER POR FECHA
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponse> obtenerPorFecha(
            LocalDate fecha
    ) {

        LocalDateTime inicio =
                fecha.atStartOfDay();

        LocalDateTime fin =
                fecha.plusDays(1)
                        .atStartOfDay();

        return compraRepository
                .findByFechaCreacionBetween(
                        inicio,
                        fin
                )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // ==========================================
    // OBTENER POR USUARIO
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponse> obtenerPorUsuario(
            Long usuarioId
    ) {

        if (!usuarioRepository.existsById(usuarioId)) {

            throw new RuntimeException(
                    "No se encontró el usuario con ID: "
                            + usuarioId
            );
        }

        return compraRepository
                .findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // ==========================================
    // COMPRAS DEL DÍA
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponse> obtenerComprasDelDia() {

        return obtenerPorFecha(
                LocalDate.now()
        );
    }

    // ==========================================
    // CANCELAR COMPRA
    // ==========================================

    @Override
    public void cancelar(Long id) {

        Compra compra =
                compraRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No se encontró la compra con ID: "
                                                + id
                                )
                        );

        // ==========================================
        // VALIDAR ESTADO
        // ==========================================

        if (compra.getEstado()
                == Compra.EstadoCompra.CANCELADA) {

            throw new RuntimeException(
                    "La compra ya está cancelada"
            );
        }

        // ==========================================
        // RESTAR STOCK
        // ==========================================

        for (CompraItem item : compra.getItems()) {

            Producto producto =
                    productoRepository.findById(
                            item.getProducto().getId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "No se encontró el producto con ID: "
                                            + item.getProducto().getId()
                            )
                    );

            // ==========================================
            // VALIDAR STOCK
            // ==========================================

            if (producto.getStock() < item.getCantidad()) {

                throw new RuntimeException(
                        "No se puede cancelar la compra porque "
                                + "el stock actual del producto "
                                + producto.getNombre()
                                + " es insuficiente para devolver "
                                + "la cantidad comprada"
                );
            }

            producto.setStock(
                    producto.getStock()
                            - item.getCantidad()
            );

            productoRepository.save(producto);
        }

        // ==========================================
        // CAMBIAR ESTADO
        // ==========================================

        compra.setEstado(
                Compra.EstadoCompra.CANCELADA
        );

        compraRepository.save(compra);
    }

    // ==========================================
    // CONVERTIR A RESPONSE
    // ==========================================

    private CompraResponse convertirAResponse(
            Compra compra
    ) {

        CompraResponse response =
                new CompraResponse();

        response.setId(compra.getId());
        response.setFolio(compra.getFolio());

        if (compra.getUsuario() != null) {

            response.setUsuarioId(
                    compra.getUsuario().getId()
            );

            response.setUsuarioNombre(
                    compra.getUsuario().getNombre()
            );

            response.setUsername(
                    compra.getUsuario().getUsername()
            );
        }

        response.setSubtotal(
                compra.getSubtotal()
        );

        response.setImpuesto(
                compra.getImpuesto()
        );

        response.setTotal(
                compra.getTotal()
        );

        response.setEstado(
                compra.getEstado()
        );

        response.setFechaCreacion(
                compra.getFechaCreacion()
        );

        List<CompraItemResponse> items =
                compra.getItems()
                        .stream()
                        .map(this::convertirItemAResponse)
                        .toList();

        response.setItems(items);

        return response;
    }

    // ==========================================
    // CONVERTIR ITEM A RESPONSE
    // ==========================================

    private CompraItemResponse convertirItemAResponse(
            CompraItem item
    ) {

        CompraItemResponse response =
                new CompraItemResponse();

        response.setId(item.getId());

        if (item.getProducto() != null) {

            response.setProductoId(
                    item.getProducto().getId()
            );

            response.setProductoNombre(
                    item.getProducto().getNombre()
            );
        }

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

            folio = "C-" +
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

        } while (
                compraRepository.existsByFolio(folio)
        );

        return folio;
    }
}
