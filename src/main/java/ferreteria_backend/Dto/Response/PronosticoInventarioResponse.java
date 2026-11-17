package ferreteria_backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PronosticoInventarioResponse {

    private Long productoId;

    private String codigo;

    private String nombre;

    private Integer stockActual;

    private Integer stockMinimo;

    private Integer unidadesVendidasUltimos30Dias;

    private BigDecimal promedioVentaDiaria;

    private Integer diasRestantes;

    private LocalDate fechaProbableAgotamiento;

    private String recomendacion;
}
