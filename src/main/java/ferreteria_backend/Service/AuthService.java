package ferreteria_backend.Service;

import ferreteria_backend.Dto.Request.LoginRequest;
import ferreteria_backend.Dto.Response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}