CREATE TABLE IF NOT EXISTS global_categories (
    global_category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    image_url TEXT,
    display_order INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    meta_data JSONB
);
