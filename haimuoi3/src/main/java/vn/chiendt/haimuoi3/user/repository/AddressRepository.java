package vn.chiendt.haimuoi3.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.user.model.postgres.AddressEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    List<AddressEntity> findByUserId(Long userId);

    Optional<AddressEntity> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<AddressEntity> findByIdAndUserId(Long id, Long userId);
}
