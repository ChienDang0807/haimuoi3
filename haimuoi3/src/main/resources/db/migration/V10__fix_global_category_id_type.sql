ALTER TABLE global_categories
    ALTER COLUMN global_category_id DROP DEFAULT;

ALTER TABLE global_categories
    ALTER COLUMN global_category_id TYPE VARCHAR(255)
    USING global_category_id::text;
