-- بيانات تجريبية للشركات (Company)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 50 LOOP
        INSERT INTO app_users (id, username, password, created_in, name, phone, deleted, url_location)
        VALUES (1000 + i, 'company' || i, '$2a$10$qNyJZwu3HE7cR.08DKjh3ut3l84muNrzYnbhY5lm1VV8DDvW4D.Dq', NOW(), 'شركة ' || i, '05900000' || i, false, 'location' || i);
        INSERT INTO companys (id, tax_rate) VALUES (1000 + i, 0);
        INSERT INTO app_user_roles (user_id, role_id) VALUES (1000 + i, 1);
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للسائقين (Driver)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 50 LOOP
        INSERT INTO app_users (id, username, password, created_in, name, phone, deleted, url_location)
        VALUES (2000 + i, 'driver' || i, '$2a$10$qNyJZwu3HE7cR.08DKjh3ut3l84muNrzYnbhY5lm1VV8DDvW4D.Dq', NOW(), 'سائق ' || i, '05800000' || i, false, 'location' || i);
        INSERT INTO drivers (id) VALUES (2000 + i);
        INSERT INTO app_user_roles (user_id, role_id) VALUES (2000 + i, 2);
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للزبائن (Customer)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 50 LOOP
        INSERT INTO app_users (id, username, password, created_in, name, phone, deleted, url_location)
        VALUES (3000 + i, 'customer' || i, '$2a$10$qNyJZwu3HE7cR.08DKjh3ut3l84muNrzYnbhY5lm1VV8DDvW4D.Dq', NOW(), 'زبون ' || i, '05700000' || i, false, 'location' || i);
        INSERT INTO customers (id) VALUES (3000 + i);
        INSERT INTO app_user_roles (user_id, role_id) VALUES (3000 + i, 3);
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للمنتجات (Product)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 50 LOOP
        INSERT INTO products (id, name, description, price, quantity, availability_status, preparation_days, is_active, average_rating, company_id)
        VALUES (4000 + i, 'منتج ' || i, 'وصف المنتج ' || i, 10.5 + i, 100 + i, 'IN_STOCK', 2, true, 5.0, 1001); -- company_id=1001 من بيانات الشركات
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للتصنيفات (Category)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 20 LOOP
        INSERT INTO categories (id, name, description, deleted, created_at, updated_at)
        VALUES (5000 + i, 'تصنيف ' || i, 'وصف التصنيف ' || i, false, NOW(), NOW());
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للطلبات (Order)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 20 LOOP
        INSERT INTO orders (id, customer_id, company_id, driver_id, status, total_price, deleted, created_at, updated_at)
        VALUES (6000 + i, 3001, 1001, 2001, 'PENDING', 100.0 + i, false, NOW(), NOW());
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية لعناصر الطلب (OrderItem)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 20 LOOP
        INSERT INTO order_items (id, order_id, quantity, base_price, tax_rate, total_price, deleted, created_at, updated_at)
        VALUES (7000 + i, 6001, 2, 20.0, 0.0, 40.0, false, NOW(), NOW());
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للعلامات (Tag)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 10 LOOP
        INSERT INTO tags (id, name, deleted, created_at, updated_at)
        VALUES (8000 + i, 'علامة ' || i, false, NOW(), NOW());
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للصور (ProductImage)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 10 LOOP
        INSERT INTO image_url (id, url) VALUES (9000 + i, 'https://example.com/image' || i || '.jpg');
        INSERT INTO product_images (id, is_main, product_id) VALUES (9000 + i, true, 4001);
        i := i + 1;
    END LOOP;
END $$;

-- بيانات تجريبية للعناوين (Address)
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 10 LOOP
        INSERT INTO address (id, street, city, state, zip_code, country)
        VALUES (10000 + i, 'شارع ' || i, 'مدينة ' || i, 'منطقة ' || i, '1234' || i, 'فلسطين');
        i := i + 1;
    END LOOP;
END $$;
