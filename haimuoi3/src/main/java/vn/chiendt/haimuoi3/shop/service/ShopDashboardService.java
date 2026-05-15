package vn.chiendt.haimuoi3.shop.service;

import vn.chiendt.haimuoi3.shop.dto.response.ShopDashboardResponse;

public interface ShopDashboardService {

    ShopDashboardResponse getDashboardForShopOwner(Long ownerUserId, String period);
}
