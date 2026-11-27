
package ferreteria_backend.Repository;

import ferreteria_backend.Entity.Documento;
import ferreteria_backend.Entity.Documento.TipoDocumento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository
        extends JpaRepository<Documento, Long> {

    Optional<Documento> findByTipo(TipoDocumento tipo);

    boolean existsByTipo(TipoDocumento tipo);

    List<Documento> findAllByOrderByTipoAsc();

}
