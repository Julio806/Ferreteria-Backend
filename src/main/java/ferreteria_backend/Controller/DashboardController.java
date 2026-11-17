package ferreteria_backend.Controller;

import ferreteria_backend.Dto.Response.DashboardResponse;
import ferreteria_backend.Service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ==========================================
    // OBTENER INFORMACIÓN DEL DASHBOARD
    // ==========================================

    @GetMapping
    public ResponseEntity<DashboardResponse> obtenerDashboard() {

        return ResponseEntity.ok(
                dashboardService.obtenerDashboard()
        );
    }
}
