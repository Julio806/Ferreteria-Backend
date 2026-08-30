package ferreteria_backend.Dto.Response;

import ferreteria_backend.Entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaResponse {

    private Long id;
    private String folio;

    private Long usuarioId;
    private String usuarioNombre;
    private String username;

    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal total;

    private Venta.MetodoPago metodoPago;
    private Venta.EstadoVenta estado;

    private LocalDateTime fechaCreacion;

    private List<VentaItemResponse> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Venta.MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(Venta.MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Venta.EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(Venta.EstadoVenta estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<VentaItemResponse> getItems() {
        return items;
    }

    public void setItems(List<VentaItemResponse> items) {
        this.items = items;
    }
}