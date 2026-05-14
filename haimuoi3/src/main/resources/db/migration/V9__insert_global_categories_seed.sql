INSERT INTO global_categories (name, slug, image_url, display_order, is_active, meta_data)
VALUES
    (
        'Electronics',
        'electronics',
        'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop',
        1,
        TRUE,
        '{"subtitle":"Smart devices for every day","ctaText":"Shop Electronics","route":"/category/electronics"}'::jsonb
    ),
    (
        'Fashion',
        'fashion',
        'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&auto=format&fit=crop',
        2,
        TRUE,
        '{"subtitle":"Modern street style","ctaText":"Shop Fashion","route":"/category/fashion"}'::jsonb
    ),
    (
        'Home Living',
        'home-living',
        'https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=800&auto=format&fit=crop',
        3,
        TRUE,
        '{"subtitle":"Comfort for your home","ctaText":"Explore Home","route":"/category/home-living"}'::jsonb
    ),
    (
        'Beauty',
        'beauty',
        'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=800&auto=format&fit=crop',
        4,
        TRUE,
        '{"subtitle":"Skincare and wellness","ctaText":"Discover Beauty","route":"/category/beauty"}'::jsonb
    ),
    (
        'Sports',
        'sports',
        'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=800&auto=format&fit=crop',
        5,
        TRUE,
        '{"subtitle":"Gear up your training","ctaText":"Shop Sports","route":"/category/sports"}'::jsonb
    ),
    (
        'Books',
        'books',
        'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=800&auto=format&fit=crop',
        6,
        TRUE,
        '{"subtitle":"Stories and knowledge","ctaText":"Browse Books","route":"/category/books"}'::jsonb
    )
ON CONFLICT (slug) DO NOTHING;
