package vn.chiendt.haimuoi3.product.model;

import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;

/**
 * Loại document trong collection {@code products}.
 * {@code null} trên BSON = dữ liệu cũ, xử lý như {@link #LEGACY}.
 */
public enum ProductKind {
    PARENT,
    SKU,
    LEGACY;

    public static ProductKind resolve(ProductEntity entity) {
        if (entity == null || entity.getProductKind() == null) {
            return LEGACY;
        }
        return entity.getProductKind();
    }
}
