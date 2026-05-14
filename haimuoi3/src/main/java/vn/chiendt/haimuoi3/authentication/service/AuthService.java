package vn.chiendt.haimuoi3.authentication.service;

import vn.chiendt.haimuoi3.authentication.dto.request.LoginRequest;
import vn.chiendt.haimuoi3.authentication.dto.request.RegisterRequest;
import vn.chiendt.haimuoi3.authentication.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    boolean validateToken(String token);
}
