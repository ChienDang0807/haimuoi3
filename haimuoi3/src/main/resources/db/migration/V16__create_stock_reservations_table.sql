CREATE TABLE stock_reservations (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT        NOT NULL,
    order_item_id  BIGINT        NOT NULL,
    shop_id        BIGINT        NOT NULL,
    product_id     VARCHAR(255)  NOT NULL,
    quantity       INT           NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at    TIMESTAMP,
    CONSTRAINT fk_stock_res_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_res_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_res_shop FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE RESTRICT,
    CONSTRAINT chk_stock_res_quantity CHECK (quantity > 0),
    CONSTRAINT chk_stock_res_status CHECK (status IN ('HELD', 'COMMITTED', 'RELEASED'))
);

CREATE UNIQUE INDEX uq_stock_res_order_item ON stock_reservations (order_item_id);
CREATE INDEX idx_stock_res_order_id ON stock_reservations (order_id);
CREATE INDEX idx_stock_res_order_status ON stock_reservations (order_id, status);
CREATE INDEX idx_stock_res_shop_product_status ON stock_reservations (shop_id, product_id, status);
