CREATE TABLE customer_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    date_of_birth DATE,
    gender VARCHAR(10),
    updated_at TIMESTAMP,
    CONSTRAINT fk_customer_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

CREATE INDEX idx_customer_profiles_user_id ON customer_profiles(user_id);
