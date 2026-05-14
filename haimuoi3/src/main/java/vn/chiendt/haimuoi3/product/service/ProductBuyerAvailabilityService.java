package vn.chiendt.haimuoi3.product.service;

import vn.chiendt.haimuoi3.product.model.ProductBuyerAvailability;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;

public interface ProductBuyerAvailabilityService {

    ProductBuyerAvailability resolveAvailability(ProductEntity entity);

    ProductBuyerAvailability resolveAvailabilityByProductId(String productId);
}
