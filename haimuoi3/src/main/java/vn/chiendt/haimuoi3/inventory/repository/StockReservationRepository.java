package vn.chiendt.haimuoi3.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.inventory.model.postgres.ReservationStatus;
import vn.chiendt.haimuoi3.inventory.model.postgres.StockReservationEntity;

import java.util.List;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservationEntity, Long> {

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM StockReservationEntity r "
            + "WHERE r.shopId = :shopId AND r.productId = :productId AND r.status = :status")
    int sumQuantityByShopIdAndProductIdAndStatus(
            @Param("shopId") Long shopId,
            @Param("productId") String productId,
            @Param("status") ReservationStatus status);

    List<StockReservationEntity> findByOrderIdAndStatus(Long orderId, ReservationStatus status);

    List<StockReservationEntity> findByOrderId(Long orderId);
}
