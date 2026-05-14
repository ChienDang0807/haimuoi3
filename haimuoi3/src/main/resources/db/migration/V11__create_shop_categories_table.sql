CREATE TABLE IF NOT EXISTS shop_categories (
    shop_category_id VARCHAR(255) PRIMARY KEY,
    shop_id VARCHAR(255) NOT NULL,
    global_category_id VARCHAR(255),
    name VARCHAR(255),
    slug VARCHAR(255) UNIQUE,
    image_url VARCHAR(500),
    display_order INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_shop_global
    ON shop_categories (shop_id, global_category_id);
