package ferreteria_backend.Service;

import ferreteria_backend.Dto.Response.DashboardResponse;

public interface DashboardService {

    // ==========================================
    // OBTENER INFORMACIÓN DEL DASHBOARD
    // ==========================================

    DashboardResponse obtenerDashboard();
}