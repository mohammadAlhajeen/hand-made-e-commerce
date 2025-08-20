-- Random test data compatible with current entities
-- Inserts companies (App_Users -> companies), customers, drivers, roles mapping, products with subtypes, categories, tags, media, images, attributes, avg_ratings, reviews, comments, relations

-- Companies
DO $$
DECLARE
  i integer := 1;
BEGIN
  WHILE i <= 10 LOOP
  INSERT INTO "app_users" (id, username, password, "created_at", name, phone, deleted, "url_location")
  VALUES (2100 + i, 'company' || i, '$2a$10$EXAMPLERANDOMHASHxxxxxx', NOW(), 'شركة تجريبية ' || i, '0592000' || i, false, 'loc' || i)
    ON CONFLICT DO NOTHING;

    INSERT INTO companies (id, tax_rate)
    VALUES (2100 + i, 0)
    ON CONFLICT DO NOTHING;

    -- assign role via app_user_roles
    INSERT INTO app_user_roles (user_id, role_id) VALUES (2100 + i, 1) ON CONFLICT DO NOTHING;

    i := i + 1;
  END LOOP;
END $$;

-- Customers
DO $$
DECLARE
  i integer := 1;
BEGIN
  WHILE i <= 20 LOOP
  INSERT INTO "app_users" (id, username, password, "created_at", name, phone, deleted, "url_location")
  VALUES (3000 + i, 'customer' || i, '$2a$10$EXAMPLERANDOMHASHxxxxxx', NOW(), 'زبون ' || i, '0573000' || i, false, NULL)
    ON CONFLICT DO NOTHING;

    INSERT INTO customers (id) VALUES (3000 + i) ON CONFLICT DO NOTHING;

    INSERT INTO app_user_roles (user_id, role_id) VALUES (3000 + i, 3) ON CONFLICT DO NOTHING;

    i := i + 1;
  END LOOP;
END $$;

-- Categories
DO $$
DECLARE
  i integer := 1;
BEGIN
  WHILE i <= 8 LOOP
    INSERT INTO categories (id, name, slug, description, deleted,  updated_at)
    VALUES (4000 + i, 'تصنيف تجريبي ' || i, 'category-' || (4000 + i), 'وصف', false,  NOW())
    ON CONFLICT DO NOTHING;
    i := i + 1;
  END LOOP;
END $$;

-- Tags
DO $$
DECLARE
  i integer := 1;
BEGIN
  WHILE i <= 8 LOOP
    INSERT INTO tags (id, name, created_at, updated_at)
    VALUES (5000 + i, 'tag-' || i, NOW(), NOW()) ON CONFLICT DO NOTHING;
    i := i + 1;
  END LOOP;
END $$;

-- Products (mix of InStock and PreOrder)
DO $$
DECLARE
  i integer := 1;
  prodId integer;
  companyId integer := 2101;
BEGIN
  WHILE i <= 30 LOOP
    prodId := 6000 + i;
    -- base product
    INSERT INTO products (id, name, description, price, is_active, average_rating, company_id, dtype, created_at, updated_at)
    VALUES (prodId, 'منتج تجريبي ' || i, 'وصف المنتج ' || i, 15 + (i % 20), true, 4.0, companyId, CASE WHEN i % 3 = 0 THEN 'PRE' ELSE 'STOCK' END, NOW(), NOW())
    ON CONFLICT DO NOTHING;

    IF i % 3 = 0 THEN
      INSERT INTO pre_order_products (id, prepaid_price, preparation_days)
      VALUES (prodId, (10 + (i % 5)), 7) ON CONFLICT DO NOTHING;
    ELSE
      INSERT INTO in_stock_products (id, quantity, returnable, return_days, allow_backorder)
      VALUES (prodId, 20 + (i % 10), true, 14, false) ON CONFLICT DO NOTHING;
    END IF;

    -- categories and tags
    INSERT INTO category_product (category_id, product_id) VALUES (4000 + ((i-1) % 8) + 1, prodId) ON CONFLICT DO NOTHING;
    INSERT INTO product_tags (product_id, tag_id) VALUES (prodId, 5000 + ((i-1) % 8) + 1) ON CONFLICT DO NOTHING;

    -- avg_ratings
    INSERT INTO avg_ratings (product_id, average_rating, rating_count, total_ratings, one_rating, two_rating, three_rating, four_rating, five_rating)
    VALUES (prodId, 4.0, 3, 3, 0,1,1,1,0) ON CONFLICT DO NOTHING;

    i := i + 1;
  END LOOP;
END $$;

-- Media items and product images
DO $$
DECLARE
  i integer := 1;
  mid uuid;
BEGIN
  WHILE i <= 30 LOOP
    mid := gen_random_uuid();
  INSERT INTO media_items (id, public_path, absolute_url, mime, user_id, width, height, size_bytes, status, created_at, last_used_at)
  VALUES (mid, '/images/test/' || i || '.jpg', 'https://cdn.example/test/' || i || '.jpg', 'image/jpeg', 2101, 800, 600, 120000, 'ACTIVE', NOW(), NOW())
    ON CONFLICT DO NOTHING;

  INSERT INTO product_images (id, media_item_id, product_id, is_main, sort_order)
  VALUES (gen_random_uuid(), mid, 6000 + i, true, 0) ON CONFLICT DO NOTHING;

    i := i + 1;
  END LOOP;
END $$;

-- Attributes minimal
DO $$
DECLARE
  i integer := 1;
BEGIN
  WHILE i <= 10 LOOP
    INSERT INTO attributes (id, name, type, is_required, product_id, updated_at)
    VALUES (7000 + i, 'الحجم', 'SELECT', false, 6001 + ((i-1) % 10), NOW()) ON CONFLICT DO NOTHING;
    i := i + 1;
  END LOOP;
END $$;

-- Reviews and comments

