ALTER TABLE orders
    ADD COLUMN checkout_batch_id UUID NULL;

CREATE INDEX idx_orders_checkout_batch_id ON orders (checkout_batch_id);
CREATE INDEX idx_orders_customer_checkout_batch ON orders (customer_id, checkout_batch_id);
