package vn.chiendt.haimuoi3.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByIdAndIsActiveTrue(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
