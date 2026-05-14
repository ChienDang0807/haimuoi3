package vn.chiendt.haimuoi3.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.product.dto.response.ProductPriceRange;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductRepositoryCustom {

    Page<ProductEntity> findGlobalProducts(
            String query,
            String globalCategoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Pageable pageable
    );

    List<ProductEntity> suggestProducts(String query, int limit);

    Page<ProductEntity> findProductsForModeration(String query, String status, Pageable pageable);

    Page<ProductEntity> findCatalogByShopId(String shopId, Pageable pageable);

    List<ProductEntity> findActiveSkusByParentIdAndShopId(String parentId, String shopId);

    Map<String, ProductPriceRange> findMinMaxActiveSkuPriceByParentIds(Collection<String> parentProductIds);

    long countByGlobalCategoryId(String globalCategoryId);
}
