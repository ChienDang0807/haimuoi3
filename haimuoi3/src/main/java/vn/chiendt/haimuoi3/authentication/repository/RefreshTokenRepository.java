package vn.chiendt.haimuoi3.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.authentication.model.postgres.RefreshTokenEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {
    
    List<RefreshTokenEntity> findByUserId(Long userId);
    
    void deleteByToken(String token);
    
    void deleteByUserId(Long userId);
    
    void deleteByExpiresAtBefore(LocalDateTime date);
}
