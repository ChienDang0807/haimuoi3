package vn.chiendt.haimuoi3.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.product.dto.request.GetProductsByIdsRequest;
import vn.chiendt.haimuoi3.product.dto.response.CartProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.ProductSuggestionResponse;
import vn.chiendt.haimuoi3.product.dto.response.ShopProductResponse;
import vn.chiendt.haimuoi3.product.service.ProductService;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/global")
    public ApiResponse<Page<GlobalProductResponse>> getGlobalProducts(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String globalCategoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @PageableDefault(size = 32) Pageable pageable
    ) {
        Page<GlobalProductResponse> products = productService.findAllGlobalProduct(
                query,
                globalCategoryId,
                minPrice,
                maxPrice,
                minRating,
                pageable
        );
        return ApiResponse.success(products, "Fetched global products");
    }

    @GetMapping("/suggest")
    public ApiResponse<List<ProductSuggestionResponse>> suggestProducts(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Integer limit
    ) {
        List<ProductSuggestionResponse> suggestions = productService.suggestProducts(query, limit);
        return ApiResponse.success(suggestions, "Fetched product suggestions");
    }

    @GetMapping("/{parentId}/skus")
    public ApiResponse<List<ShopProductResponse>> listSkusByParent(@PathVariable String parentId) {
        List<ShopProductResponse> skus = productService.listActiveSkusByParentForPublic(parentId);
        return ApiResponse.success(skus, "Fetched product SKUs");
    }

    @GetMapping("/{id}")
    public ApiResponse<ShopProductResponse> getProductById(@PathVariable String id) {
        ShopProductResponse product = productService.findActiveProductForPublic(id);
        return ApiResponse.success(product, "Fetched product");
    }

    @PostMapping("/cart/batch")
    public ApiResponse<List<CartProductResponse>> getCartProductsByIds(
            @RequestBody GetProductsByIdsRequest request
    ) {
        List<CartProductResponse> products = productService.findCartProductsByIds(request.getIds());
        return ApiResponse.success(products, "Fetched cart products by ids");
    }
}
