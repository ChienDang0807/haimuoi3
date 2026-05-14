-- Insert shop owner users (password: 123456)
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye/8RzCBRqRVHJLjKCJy.zQYzQjOQz8vS
INSERT INTO users (email, phone, password_hash, role, full_name, is_active, is_verified, created_at)
VALUES
    ('owner1@shop.com', '0901234561', '$2a$10$N9qo8uLOickgx2ZMRZoMye/8RzCBRqRVHJLjKCJy.zQYzQjOQz8vS', 'SHOP_OWNER', 'Nguyễn Văn Shop 1', true, true, CURRENT_TIMESTAMP),
    ('owner2@shop.com', '0901234562', '$2a$10$N9qo8uLOickgx2ZMRZoMye/8RzCBRqRVHJLjKCJy.zQYzQjOQz8vS', 'SHOP_OWNER', 'Trần Thị Shop 2', true, true, CURRENT_TIMESTAMP),
    ('owner3@shop.com', '0901234563', '$2a$10$N9qo8uLOickgx2ZMRZoMye/8RzCBRqRVHJLjKCJy.zQYzQjOQz8vS', 'SHOP_OWNER', 'Lê Văn Shop 3', true, true, CURRENT_TIMESTAMP);

-- Insert customer users (password: 123456)
INSERT INTO users (email, phone, password_hash, role, full_name, is_active, is_verified, created_at)
VALUES
    ('customer1@mail.com', '0912345671', '$2a$10$N9qo8uLOickgx2ZMRZoMye/8RzCBRqRVHJLjKCJy.zQYzQjOQz8vS', 'CUSTOMER', 'Phạm Văn Khách 1', true, true, CURRENT_TIMESTAMP),
    ('customer2@mail.com', '0912345672', '$2a$10$N9qo8uLOickgx2ZMRZoMye/8RzCBRqRVHJLjKCJy.zQYzQjOQz8vS', 'CUSTOMER', 'Hoàng Thị Khách 2', true, true, CURRENT_TIMESTAMP);

-- Insert shops (gắn với shop owners)
INSERT INTO shops (owner_id, shop_name, slug, description, logo_url, banner_url, email, phone, province, district, address_detail, status, created_at)
VALUES
    (
        (SELECT id FROM users WHERE email = 'owner1@shop.com'),
        'Shop Điện Tử Số 1',
        'shop-dien-tu-so-1',
        'Chuyên cung cấp các thiết bị điện tử chất lượng cao với giá cả cạnh tranh',
        'https://via.placeholder.com/150',
        'https://via.placeholder.com/800x200',
        'contact@shop-dien-tu-1.com',
        '0281234561',
        'TP. Hồ Chí Minh',
        'Quận 1',
        '123 Nguyễn Huệ',
        'ACTIVE',
        CURRENT_TIMESTAMP
    ),
    (
        (SELECT id FROM users WHERE email = 'owner2@shop.com'),
        'Shop Thời Trang Trendy',
        'shop-thoi-trang-trendy',
        'Thời trang trẻ trung, năng động với phong cách hiện đại',
        'https://via.placeholder.com/150',
        'https://via.placeholder.com/800x200',
        'contact@trendy-fashion.com',
        '0281234562',
        'Hà Nội',
        'Quận Ba Đình',
        '456 Hoàng Hoa Thám',
        'ACTIVE',
        CURRENT_TIMESTAMP
    ),
    (
        (SELECT id FROM users WHERE email = 'owner3@shop.com'),
        'Shop Nội Thất Home Decor',
        'shop-noi-that-home-decor',
        'Nội thất cao cấp cho ngôi nhà hiện đại của bạn',
        'https://via.placeholder.com/150',
        'https://via.placeholder.com/800x200',
        'contact@homedecor.com',
        '0281234563',
        'Đà Nẵng',
        'Quận Hải Châu',
        '789 Lê Duẩn',
        'ACTIVE',
        CURRENT_TIMESTAMP
    );

-- Insert sample customer profiles
INSERT INTO customer_profiles (user_id, date_of_birth, gender, updated_at)
VALUES
    (
        (SELECT id FROM users WHERE email = 'customer1@mail.com'),
        '1995-05-15',
        'MALE',
        CURRENT_TIMESTAMP
    ),
    (
        (SELECT id FROM users WHERE email = 'customer2@mail.com'),
        '1998-08-20',
        'FEMALE',
        CURRENT_TIMESTAMP
    );

-- Insert sample addresses for customers
INSERT INTO addresses (user_id, address_name, recipient_name, phone, province, district, ward, street_address, is_default, created_at)
VALUES
    (
        (SELECT id FROM users WHERE email = 'customer1@mail.com'),
        'Nhà',
        'Phạm Văn Khách 1',
        '0912345671',
        'TP. Hồ Chí Minh',
        'Quận 3',
        'Phường 6',
        '234 Võ Văn Tần',
        true,
        CURRENT_TIMESTAMP
    ),
    (
        (SELECT id FROM users WHERE email = 'customer1@mail.com'),
        'Công ty',
        'Phạm Văn Khách 1',
        '0912345671',
        'TP. Hồ Chí Minh',
        'Quận 1',
        'Phường Bến Nghé',
        '100 Lê Lợi',
        false,
        CURRENT_TIMESTAMP
    ),
    (
        (SELECT id FROM users WHERE email = 'customer2@mail.com'),
        'Nhà',
        'Hoàng Thị Khách 2',
        '0912345672',
        'Hà Nội',
        'Quận Đống Đa',
        'Phường Láng Hạ',
        '567 Láng Hạ',
        true,
        CURRENT_TIMESTAMP
    );
