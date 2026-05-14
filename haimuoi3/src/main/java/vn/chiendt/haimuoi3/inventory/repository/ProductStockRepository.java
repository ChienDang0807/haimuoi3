package vn.chiendt.haimuoi3.inventory.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.inventory.model.postgres.ProductStockEntity;

import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStockEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductStockEntity p WHERE p.shopId = :shopId AND p.productId = :productId")
    Optional<ProductStockEntity> findByShopIdAndProductIdForUpdate(
            @Param("shopId") Long shopId,
            @Param("productId") String productId);

    Optional<ProductStockEntity> findByShopIdAndProductId(Long shopId, String productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "INSERT INTO product_stock (shop_id, product_id, quantity_on_hand, version, created_at) "
                    + "VALUES (:shopId, :productId, 0, 0, CURRENT_TIMESTAMP) "
                    + "ON CONFLICT (shop_id, product_id) DO NOTHING",
            nativeQuery = true)
    void ensureRowExists(@Param("shopId") Long shopId, @Param("productId") String productId);
}
