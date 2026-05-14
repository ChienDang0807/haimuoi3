package vn.chiendt.haimuoi3.shop.service;

import vn.chiendt.haimuoi3.shop.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;

public interface ShopService {

    ShopResponse getShopByOwnerId(Long ownerId);

    ShopResponse updateShop(Long ownerId, UpdateShopRequest request);

    ShopResponse getShopBySlug(String slug);

    ShopResponse getShopById(Long id);
}
