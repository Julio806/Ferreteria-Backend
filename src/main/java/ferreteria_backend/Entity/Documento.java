
package ferreteria_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "documentos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "tipo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 40)
    private TipoDocumento tipo;

    @Column(nullable = false, length = 255)
    private String nombreArchivo;

    @Column(nullable = false, length = 500)
    private String rutaArchivo;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        fechaSubida = LocalDateTime.now();
    }

    public enum TipoDocumento {
        VENTAS_WORD,
        VENTAS_EXCEL,
        INVENTARIO_WORD,
        INVENTARIO_EXCEL
    }
}
