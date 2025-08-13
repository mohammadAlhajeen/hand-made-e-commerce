-- بيانات 10 شركات يدوية
DO $$
DECLARE
    i integer := 1;
BEGIN
    WHILE i <= 10 LOOP
        INSERT INTO app_users (id, username, password, created_in, name, phone, deleted, url_location)
        VALUES (1100 + i, 'company' || (i + 100), '$2a$10$qNyJZwu3HE7cR.08DKjh3ut3l84muNrzYnbhY5lm1VV8DDvW4D.Dq', NOW(), 'شركة يدوية ' || i, '05910000' || i, false, 'location' || i);
        INSERT INTO companys (id, tax_rate) VALUES (1100 + i, 0);
        INSERT INTO app_user_roles (user_id, role_id) VALUES (1100 + i, 1);
        i := i + 1;
    END LOOP;
END $$;

-- بيانات 200 منتج واقعي موزعة على 10 شركات
DO $$
DECLARE
    i integer := 1;
    company_idx integer;
    product_id integer;
    company_id integer;
    product_name text;
    product_desc text;
    price numeric;
    cat_id integer;
    tag_id1 integer;
    tag_id2 integer;
BEGIN
    WHILE i <= 200 LOOP
        company_idx := ((i - 1) / 20) + 1; -- كل 20 منتج لشركة
        company_id := 1100 + company_idx;
        product_id := 5000 + i;
        -- أسماء واقعية متنوعة
        product_name := CASE (i % 10)
            WHEN 1 THEN 'سوار مطرز يدوي'
            WHEN 2 THEN 'لوحة ديكور خشبية'
            WHEN 3 THEN 'محفظة تطريز فلسطيني'
            WHEN 4 THEN 'صابون زيت الزيتون الطبيعي'
            WHEN 5 THEN 'قلادة تراثية'
            WHEN 6 THEN 'علبة مجوهرات خشبية'
            WHEN 7 THEN 'وسادة مطرزة'
            WHEN 8 THEN 'شمعة معطرة طبيعية'
            WHEN 9 THEN 'حقيبة يد قماشية'
            ELSE 'ميدالية مفاتيح خشبية'
        END;
        product_desc := product_name || ' رقم ' || i;
        price := 10 + (i % 50);
        -- التصنيف (5 تصنيفات)
        cat_id := 5010 + ((i - 1) % 5);
    -- علامتان لكل منتج (من 10 علامات: 8001-8010)
    tag_id1 := 8000 + ((i - 1) % 10) + 1;
    tag_id2 := 8000 + ((i + 2) % 10) + 1;
        INSERT INTO products (id, name, description, price, quantity, availability_status, preparation_days, is_active, average_rating, company_id)
        VALUES (product_id, product_name, product_desc, price, 100 + (i % 30), 'IN_STOCK', 2, true, 4.5 + ((i % 5) * 0.1), company_id);
        INSERT INTO category_product (category_id, product_id) VALUES (cat_id, product_id);
        INSERT INTO product_tags (product_id, tag_id) VALUES (product_id, tag_id1);
        INSERT INTO product_tags (product_id, tag_id) VALUES (product_id, tag_id2);
        i := i + 1;
    END LOOP;
END $$;
