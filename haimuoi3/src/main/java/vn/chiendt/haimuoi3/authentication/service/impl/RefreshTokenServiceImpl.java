package vn.chiendt.haimuoi3.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.authentication.model.postgres.RefreshTokenEntity;
import vn.chiendt.haimuoi3.authentication.repository.RefreshTokenRepository;
import vn.chiendt.haimuoi3.authentication.service.RefreshTokenService;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RefreshTokenEntity createRefreshToken(Long userId, String deviceInfo, String ipAddress) {
        String token = jwtTokenProvider.generateRefreshToken();
        long refreshTokenExpiration = jwtTokenProvider.getRefreshTokenExpiration();
        
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .token(token)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration))
                .createdAt(LocalDateTime.now())
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshTokenEntity validateRefreshToken(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findById(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Override
    @Transactional
    public void deleteAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
