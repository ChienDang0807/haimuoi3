package vn.chiendt.haimuoi3.order.model.postgres;

public enum OrderStatus {
    PENDING,
    PENDING_PAYMENT,
    CONFIRMED,
    PAID,
    READY_TO_SHIP,
    PAYMENT_FAILED,
    SHIPPING,
    DELIVERED,
    CANCELLED
}
