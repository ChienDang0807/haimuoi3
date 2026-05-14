package vn.chiendt.haimuoi3.authentication.service;

import vn.chiendt.haimuoi3.authentication.model.postgres.RefreshTokenEntity;

public interface RefreshTokenService {
    
    RefreshTokenEntity createRefreshToken(Long userId, String deviceInfo, String ipAddress);
    
    RefreshTokenEntity validateRefreshToken(String token);
    
    void deleteRefreshToken(String token);
    
    void deleteAllUserTokens(Long userId);
    
    void cleanupExpiredTokens();
}
