package vn.chiendt.haimuoi3.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.user.model.postgres.CustomerProfileEntity;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfileEntity, Long> {

    Optional<CustomerProfileEntity> findByUserId(Long userId);
}
