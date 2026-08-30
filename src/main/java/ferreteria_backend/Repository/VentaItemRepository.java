package ferreteria_backend.Repository;

import ferreteria_backend.Entity.VentaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaItemRepository
        extends JpaRepository<VentaItem, Long> {
}