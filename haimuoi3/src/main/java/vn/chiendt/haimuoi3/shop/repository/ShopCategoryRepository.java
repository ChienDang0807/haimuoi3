package vn.chiendt.haimuoi3.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.chiendt.haimuoi3.shop.model.ShopCategoryEntity;

public interface ShopCategoryRepository extends JpaRepository<ShopCategoryEntity, String> {

    boolean existsByShopIdAndSlug(String shopId, String slug);

    long countByShopId(String shopId);

    Page<ShopCategoryEntity> findByShopIdOrderByDisplayOrderAsc(String shopId, Pageable pageable);
}
