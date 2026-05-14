package vn.chiendt.haimuoi3.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;

import java.util.List;

public interface ProductRepository extends MongoRepository<ProductEntity, String>, ProductRepositoryCustom {

    Page<ProductEntity> findByShopId(String shopId, Pageable pageable);

    List<ProductEntity> findByParentProductIdAndShopIdAndStatusIgnoreCaseOrderBySkuAsc(
            String parentProductId, String shopId, String status);

    boolean existsByShopIdAndSku(String shopId, String sku);

    boolean existsByShopIdAndSkuAndIdNot(String shopId, String sku, String id);

    List<ProductEntity> findByIdIn(List<String> ids);
}
