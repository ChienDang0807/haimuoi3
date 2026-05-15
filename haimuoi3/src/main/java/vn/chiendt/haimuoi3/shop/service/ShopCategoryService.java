package vn.chiendt.haimuoi3.shop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.shop.dto.request.CreateShopCategoryRequest;
import vn.chiendt.haimuoi3.shop.dto.request.UpdateShopCategoryRequest;
import vn.chiendt.haimuoi3.shop.dto.response.ShopCategoryResponse;

public interface ShopCategoryService {

    Page<ShopCategoryResponse> listForShopOwner(Long ownerUserId, Pageable pageable);

    ShopCategoryResponse createShopCategory(Long ownerUserId, CreateShopCategoryRequest request);

    ShopCategoryResponse updateShopCategory(Long ownerUserId, String shopCategoryId, UpdateShopCategoryRequest request);

    ShopCategoryResponse toggleActive(Long ownerUserId, String shopCategoryId);

    void deleteShopCategory(Long ownerUserId, String shopCategoryId);
}
