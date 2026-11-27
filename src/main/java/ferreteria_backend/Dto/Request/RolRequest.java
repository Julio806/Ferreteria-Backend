package ferreteria_backend.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RolRequest {

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String nombre;
}
