CREATE TABLE wishlist_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wishlist_items_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_wishlist_items_user_created ON wishlist_items (user_id, created_at DESC);
