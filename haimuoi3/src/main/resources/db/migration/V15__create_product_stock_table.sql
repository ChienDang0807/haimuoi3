CREATE TABLE product_stock (
    id                 BIGSERIAL PRIMARY KEY,
    shop_id            BIGINT        NOT NULL,
    product_id         VARCHAR(255)  NOT NULL,
    quantity_on_hand   INT           NOT NULL DEFAULT 0,
    version            BIGINT        NOT NULL DEFAULT 0,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    CONSTRAINT fk_product_stock_shop FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE CASCADE,
    CONSTRAINT chk_product_stock_qty CHECK (quantity_on_hand >= 0),
    CONSTRAINT uq_product_stock_shop_product UNIQUE (shop_id, product_id)
);

CREATE INDEX idx_product_stock_shop_id ON product_stock (shop_id);
CREATE INDEX idx_product_stock_product_id ON product_stock (product_id);
