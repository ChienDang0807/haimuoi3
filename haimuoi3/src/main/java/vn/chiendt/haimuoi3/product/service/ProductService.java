package vn.chiendt.haimuoi3.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.chiendt.haimuoi3.product.dto.request.CreateProductRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateProductRequest;
import vn.chiendt.haimuoi3.product.dto.response.CartProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.ProductSuggestionResponse;
import vn.chiendt.haimuoi3.product.dto.response.ShopProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Page<GlobalProductResponse> findAllGlobalProduct(
            String query,
            String globalCategoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Pageable pageable
    );

    List<ProductSuggestionResponse> suggestProducts(String query, Integer limit);

    /**
     * Chi tiết sản phẩm công khai: chỉ {@code ACTIVE}.
     */
    ShopProductResponse findActiveProductForPublic(String id);

    /**
     * SKU đang ACTIVE của một PARENT (public).
     */
    List<ShopProductResponse> listActiveSkusByParentForPublic(String parentId);

    List<CartProductResponse> findCartProductsByIds(List<String> ids);

    Page<ShopProductResponse> findAllShopProduct(Pageable pageable);

    ShopProductResponse findById(String id);

    ShopProductResponse save(CreateProductRequest request);

    ShopProductResponse update(String id, UpdateProductRequest request);

    ShopProductResponse uploadProductImage(String id, MultipartFile file);

    ShopProductResponse uploadProductImages(String id, MultipartFile[] files);

    void delete(String id);

    Page<ShopProductResponse> findAllByShopId(String shopId, Pageable pageable);


}
