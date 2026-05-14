-- Sửa password seed: V5 ghi chú "123456" nhưng hash cũ không verify được với BCrypt.
-- Mật khẩu thống nhất sau migration: 123456
-- Hash BCrypt cost 10 ($2b$), đã verify bằng thư viện bcrypt (Python).

UPDATE users
SET password_hash = '$2b$10$PVyeECLcroNR7cJKo30yE.fTqJSu/dV9K/Mxm1GB0VdvghBWwgW36',
    updated_at    = CURRENT_TIMESTAMP
WHERE email IN (
                'owner1@shop.com',
                'owner2@shop.com',
                'owner3@shop.com',
                'customer1@mail.com',
                'customer2@mail.com'
    );

-- Tài khoản bổ sung cho QA (trùng email thì chỉ refresh hash / trạng thái)
INSERT INTO users (email, phone, password_hash, role, full_name, is_active, is_verified, created_at)
VALUES ('test.customer@haimuoi3.local', '0999999001',
        '$2b$10$PVyeECLcroNR7cJKo30yE.fTqJSu/dV9K/Mxm1GB0VdvghBWwgW36', 'CUSTOMER',
        'Test Customer', true, true, CURRENT_TIMESTAMP),
       ('test.shopowner@haimuoi3.local', '0999999002',
        '$2b$10$PVyeECLcroNR7cJKo30yE.fTqJSu/dV9K/Mxm1GB0VdvghBWwgW36', 'SHOP_OWNER',
        'Test Shop Owner', true, true, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    phone           = EXCLUDED.phone,
    full_name       = EXCLUDED.full_name,
    role            = EXCLUDED.role,
    is_active       = EXCLUDED.is_active,
    is_verified     = EXCLUDED.is_verified,
    updated_at      = CURRENT_TIMESTAMP;

INSERT INTO customer_profiles (user_id, date_of_birth, gender, updated_at)
SELECT u.id, DATE '2000-01-01', 'OTHER', CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'test.customer@haimuoi3.local'
  AND NOT EXISTS (SELECT 1 FROM customer_profiles cp WHERE cp.user_id = u.id);

INSERT INTO shops (owner_id, shop_name, slug, description, logo_url, banner_url, email, phone, province,
                   district, address_detail, status, created_at)
SELECT u.id,
       'Test Shop Haimuoi3',
       'test-shop-haimuoi3-local',
       'Seed cho đăng nhập shop owner test',
       'https://via.placeholder.com/150',
       'https://via.placeholder.com/800x200',
       'shop@test.haimuoi3.local',
       '0999999002',
       'Hà Nội',
       'Ba Đình',
       '1 Test Street',
       'ACTIVE',
       CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'test.shopowner@haimuoi3.local'
  AND NOT EXISTS (SELECT 1 FROM shops s WHERE s.owner_id = u.id);
