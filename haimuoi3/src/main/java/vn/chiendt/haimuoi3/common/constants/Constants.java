package vn.chiendt.haimuoi3.common.constants;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

import java.util.EnumSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Cart {
        public static final int MAX_QUANTITY_PER_ITEM = 999;
        public static final int MAX_DISTINCT_ITEMS = 100;
        public static final int GUEST_CART_TTL_DAYS = 30;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class User {
        public static final int MIN_PASSWORD_LENGTH = 6;
        public static final int MAX_PASSWORD_LENGTH = 50;
        public static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Address {
        public static final int MAX_ADDRESS_NAME_LENGTH = 255;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Shop {
        public static final int MAX_SHOP_NAME_LENGTH = 150;
        public static final String SLUG_REGEX = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Product {
        public static final int MAX_SEARCH_QUERY_LENGTH = 50;
        public static final int DEFAULT_SUGGEST_LIMIT = 8;
        public static final int MAX_SUGGEST_LIMIT = 10;
        public static final int MAX_SKU_CODE_LENGTH = 64;
        /** Thông báo khi client dùng id document PARENT cho giỏ / đơn. */
        public static final String PARENT_NOT_SELLABLE = "Catalog parent product cannot be purchased; select a variant (SKU)";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Review {
        public static final int MAX_COMMENT_LENGTH = 1000;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Jwt {
        public static final int TOKEN_EXPIRATION_HOURS = 1;
        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String HEADER_STRING = "Authorization";
    }

    /** Quy uoc don hang (phan trang, transition khach). */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Order {
        public static final int MAX_PAGE_SIZE = 100;

        /**
         * Trang thai cho phep khach huy don: chi PENDING va CONFIRMED.
         * Khong cho PENDING_PAYMENT / PAID (thanh toan online + refund phuc tap).
         */
        public static final Set<OrderStatus> CUSTOMER_CANCELLABLE_STATUSES = EnumSet.of(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED
        );
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Inventory {
        public static final String INSUFFICIENT_STOCK = "Not enough stock for one or more products";
        public static final String PRODUCT_SHOP_MISMATCH = "Product does not belong to this shop";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Wishlist {
        public static final int DEFAULT_RECENT_LIMIT = 3;
        public static final int MAX_RECENT_LIMIT = 10;
        public static final int MAX_CONTAINS_IDS = 100;
    }
}