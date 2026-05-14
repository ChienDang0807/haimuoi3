-- Slug unique theo shop (nhiều shop được cùng slug); bỏ unique chỉ trên slug.
ALTER TABLE shop_categories DROP CONSTRAINT IF EXISTS shop_categories_slug_key;

ALTER TABLE shop_categories
    ADD CONSTRAINT uq_shop_categories_shop_id_slug UNIQUE (shop_id, slug);
