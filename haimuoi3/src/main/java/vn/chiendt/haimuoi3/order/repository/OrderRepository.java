package vn.chiendt.haimuoi3.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    List<OrderEntity> findByShopId(Long shopId);

    Page<OrderEntity> findByShopId(Long shopId, Pageable pageable);
    
    List<OrderEntity> findByCustomerId(Long customerId);

    Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);
    
    List<OrderEntity> findByShopIdAndStatus(Long shopId, OrderStatus status);
    
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<OrderEntity> findByIdAndCustomerId(Long id, Long customerId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.customerId = :customerId")
    Optional<OrderEntity> findByIdAndCustomerIdWithItems(@Param("id") Long id, @Param("customerId") Long customerId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.shopId = :shopId")
    Optional<OrderEntity> findByIdAndShopId(@Param("id") Long id, @Param("shopId") Long shopId);

    List<OrderEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<OrderEntity> findByShopIdAndCreatedAtBetween(Long shopId, LocalDateTime from, LocalDateTime to);

    List<OrderEntity> findByShopIdOrderByCreatedAtDesc(Long shopId);

    boolean existsByCheckoutBatchId(UUID checkoutBatchId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.shopId = :shopId AND o.createdAt >= :from AND o.createdAt < :to")
    List<OrderEntity> findByShopIdAndCreatedAtBetweenWithItems(@Param("shopId") Long shopId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT o FROM OrderEntity o WHERE o.shopId = :shopId AND o.createdAt < :before AND o.customerId IN :customerIds")
    List<OrderEntity> findByShopIdAndCreatedAtBeforeAndCustomerIdIn(@Param("shopId") Long shopId, @Param("before") LocalDateTime before, @Param("customerIds") Collection<Long> customerIds);
}
