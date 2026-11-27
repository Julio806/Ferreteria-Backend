
package ferreteria_backend.Service;

import java.time.LocalDate;

public interface GeneradorReporteService {

    // VENTAS WORD
    byte[] generarVentasWord(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    );

    // VENTAS EXCEL
    byte[] generarVentasExcel(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    );

    // INVENTARIO WORD
    byte[] generarInventarioWord();

    // INVENTARIO EXCEL
    byte[] generarInventarioExcel();
}
