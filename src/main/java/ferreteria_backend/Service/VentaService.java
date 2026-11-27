package ferreteria_backend.Service;

import ferreteria_backend.Dto.Request.VentaRequest;
import ferreteria_backend.Dto.Response.VentaResponse;

import java.time.LocalDate;
import java.util.List;

public interface VentaService {

    VentaResponse crear(VentaRequest request);

    VentaResponse obtenerPorId(Long id);

    List<VentaResponse> obtenerTodas();

    List<VentaResponse> obtenerPorFecha(LocalDate fecha);

    List<VentaResponse> obtenerPorUsuario(Long usuarioId);

    List<VentaResponse> obtenerVentasDelDia();

    void cancelar(Long id);
}
