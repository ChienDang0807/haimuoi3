package vn.chiendt.haimuoi3.product.model;

/**
 * Trang thai hien thi cho nguoi mua (wishlist, catalog). Tinh tu product + product_stock.
 */
public enum ProductBuyerAvailability {
    /** Con ban va co ton ban duoc (> 0) theo rule SKU/parent. */
    AVAILABLE,
    /** Listing con ACTIVE nhung khong con ton ban duoc. */
    OUT_OF_STOCK,
    /** Khong ACTIVE, khong tim thay, hoac khong hop le de ban. */
    DISCONTINUED
}
