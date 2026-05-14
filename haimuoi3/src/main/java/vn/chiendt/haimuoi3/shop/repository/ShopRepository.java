package vn.chiendt.haimuoi3.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, Long> {

    Optional<ShopEntity> findByOwnerId(Long ownerId);

    Optional<ShopEntity> findBySlug(String slug);

    Optional<ShopEntity> findByIdAndOwnerId(Long id, Long ownerId);
}
