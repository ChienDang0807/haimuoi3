ALTER TABLE orders
    ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'COD',
    ADD COLUMN shipping_address TEXT,
    ADD COLUMN stripe_session_id VARCHAR(255);
