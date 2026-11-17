package ferreteria_backend.Dto.Response;

import java.math.BigDecimal;

public interface ProductoMovimientoProjection {

    Long getProductoId();

    String getNombreProducto();

    Long getUnidades();

    BigDecimal getImporte();
}
