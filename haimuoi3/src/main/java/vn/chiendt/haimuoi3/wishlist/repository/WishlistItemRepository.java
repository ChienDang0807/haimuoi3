package vn.chiendt.haimuoi3.wishlist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.wishlist.model.postgres.WishlistItemEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, Long> {

    Optional<WishlistItemEntity> findByUserIdAndProductId(Long userId, String productId);

    void deleteByUserIdAndProductId(Long userId, String productId);

    List<WishlistItemEntity> findByUserIdAndProductIdIn(Long userId, Collection<String> productIds);

    Page<WishlistItemEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
