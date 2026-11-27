
package ferreteria_backend.Service;

import ferreteria_backend.Entity.Documento;
import ferreteria_backend.Entity.Documento.TipoDocumento;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentoService {

    Documento subirPlantilla(
            TipoDocumento tipo,
            MultipartFile archivo
    );

    List<Documento> obtenerTodos();

    Documento obtenerPorTipo(TipoDocumento tipo);

    void eliminar(TipoDocumento tipo);

}
