package ferreteria_backend.Dto.Request;

import ferreteria_backend.Entity.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class VentaRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El método de pago es obligatorio")
    private Venta.MetodoPago metodoPago;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Valid
    private List<VentaItemRequest> items;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Venta.MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(Venta.MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<VentaItemRequest> getItems() {
        return items;
    }

    public void setItems(List<VentaItemRequest> items) {
        this.items = items;
    }
}