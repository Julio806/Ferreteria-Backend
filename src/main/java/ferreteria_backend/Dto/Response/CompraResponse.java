package ferreteria_backend.Dto.Response;

import ferreteria_backend.Entity.Compra;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponse {

    private Long id;

    private String folio;

    private Long usuarioId;

    private String usuarioNombre;

    private String username;

    private BigDecimal subtotal;

    private BigDecimal impuesto;

    private BigDecimal total;

    private Compra.EstadoCompra estado;

    private LocalDateTime fechaCreacion;

    private List<CompraItemResponse> items;
}
