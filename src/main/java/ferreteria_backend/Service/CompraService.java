package ferreteria_backend.Service;

import ferreteria_backend.Dto.Request.CompraRequest;
import ferreteria_backend.Dto.Response.CompraResponse;

import java.time.LocalDate;
import java.util.List;
//Aplica las reglas que manda ah llamar los repositorios
public interface CompraService {

    CompraResponse crear(CompraRequest request);

    CompraResponse obtenerPorId(Long id);

    List<CompraResponse> obtenerTodas();

    List<CompraResponse> obtenerPorFecha(LocalDate fecha);

    List<CompraResponse> obtenerPorUsuario(Long usuarioId);

    List<CompraResponse> obtenerComprasDelDia();

    void cancelar(Long id);
}
