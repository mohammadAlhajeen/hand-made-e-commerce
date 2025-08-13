-- امتدادات
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =====================================================================
-- 1) جدول فهرسة منفصل
--    نخزّن فيه أعمدة TSVECTOR لكل جزء + عمود مجمّع للفهرسة
-- =====================================================================
CREATE OR REPLACE FUNCTION search_products_cards(
  p_query  text,
  p_limit  int  DEFAULT 20,
  p_offset int  DEFAULT 0
)
RETURNS TABLE (
  id               bigint,
  name             text,
  description      text,
  price            numeric,
  "mainImageUrl"   text,
  "preparationDays" int
)
LANGUAGE sql
STABLE
AS $$
WITH q AS (
  SELECT
    ar_en_tsquery(p_query)   AS tsq,
    ar_normalize(p_query)    AS nq_ar,
    en_normalize(p_query)    AS nq_en
),
ranks AS (
  SELECT
    p.id,
    p.name,
    p.description,
    p.price,
    p.preparation_days,

    -- subquery لجلب الصورة الرئيسية
    (
      SELECT iu.url
      FROM image_url iu join product_images pi 
	  on iu.id= pi.id
      WHERE pi.product_id = p.id
        AND pi.is_main = true
      LIMIT 1
    ) AS main_image_url,

    (
        1.00 * ts_rank_cd(ps.fts_name,        q.tsq) +
        0.80 * ts_rank_cd(ps.fts_tags,        q.tsq) +
        0.60 * ts_rank_cd(ps.fts_company,     q.tsq) +
        0.40 * ts_rank_cd(ps.fts_categories,  q.tsq) +
        0.20 * ts_rank_cd(ps.fts_description, q.tsq) +
        0.60 * GREATEST(
              similarity(ar_normalize(p.name),        q.nq_ar),
              similarity(en_normalize(p.name),        q.nq_en),
              similarity(ar_normalize(p.description), q.nq_ar),
              similarity(en_normalize(p.description), q.nq_en)
        )
    )::numeric AS score
  FROM product_search ps
  JOIN products p ON p.id = ps.product_id
  CROSS JOIN q
  WHERE ps.fts_all @@ q.tsq OR similarity(ar_normalize(p.name), q.nq_ar) > 0.3
OR similarity(en_normalize(p.name), q.nq_en) > 0.3

)
SELECT
  id,
  name,
  description,
  price,
  main_image_url   AS "mainImageUrl",
  preparation_days AS "preparationDays"
FROM ranks
ORDER BY score DESC, id
LIMIT p_limit OFFSET p_offset;
$$;


-- فهرس GIN على المجمّع
CREATE INDEX IF NOT EXISTS idx_product_search_fts_all
  ON product_search USING gin(fts_all);

-- =====================================================================
-- 2) توابع مساعدِة (نصّ التاجات/الفئات/الشركة)
-- =====================================================================
CREATE OR REPLACE FUNCTION get_product_tags_text(p_id BIGINT)
RETURNS TEXT
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT COALESCE(string_agg(t.name, ' ' ORDER BY t.id), '')
  FROM product_tags pt
  JOIN tags t ON t.id = pt.tag_id
  WHERE pt.product_id = p_id
    AND (t.deleted IS NULL OR t.deleted = false)
$$;

CREATE OR REPLACE FUNCTION get_product_categories_text(p_id BIGINT)
RETURNS TEXT
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT COALESCE(string_agg(c.name, ' ' ORDER BY c.id), '')
  FROM category_product cp
  JOIN categories c ON c.id = cp.category_id
  WHERE cp.product_id = p_id
    AND (c.deleted IS NULL OR c.deleted = false)
$$;

-- اسم الشركة المرتبط بالمنتج
CREATE OR REPLACE FUNCTION get_product_company_text(p_id BIGINT)
RETURNS TEXT
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT COALESCE(au.name, '')
  FROM products p
  LEFT JOIN companies comp ON comp.id = p.company_id
  join app_users au on au.id=comp.id
  WHERE p.id = p_id
$$;

-- =====================================================================
-- 3) تحديث/إعادة بناء صف الفهرسة لمنتج واحد (UPSERT)
-- =====================================================================
CREATE OR REPLACE FUNCTION refresh_product_search(p_id BIGINT)
RETURNS VOID
LANGUAGE sql
AS $$
  INSERT INTO product_search (
    product_id, fts_name, fts_tags, fts_company, fts_categories, fts_description
  )
  SELECT
    p.id,
    ar_en_tsvector(COALESCE(p.name,'')),
    ar_en_tsvector(get_product_tags_text(p.id)),
    ar_en_tsvector(get_product_company_text(p.id)),
    ar_en_tsvector(get_product_categories_text(p.id)),
    ar_en_tsvector(COALESCE(p.description,''))
  FROM products p
  WHERE p.id = p_id
  ON CONFLICT (product_id) DO UPDATE SET
    fts_name        = EXCLUDED.fts_name,
    fts_tags        = EXCLUDED.fts_tags,
    fts_company     = EXCLUDED.fts_company,
    fts_categories  = EXCLUDED.fts_categories,
    fts_description = EXCLUDED.fts_description;
$$;


-- =====================================================================
-- 4) Triggers لتحديث الفهرسة عند أي تغيير
-- =====================================================================
-- على جدول المنتجات: إدراج/تعديل name, description, company_id
CREATE OR REPLACE FUNCTION trg_products_to_search()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  PERFORM refresh_product_search(NEW.id);
  RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS t_products_to_search ON products;
CREATE TRIGGER t_products_to_search
AFTER INSERT OR UPDATE OF name, description, company_id ON products
FOR EACH ROW EXECUTE FUNCTION trg_products_to_search();

-- تغيّر روابط التاجات
CREATE OR REPLACE FUNCTION trg_product_tags_to_search()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE p_id BIGINT;
BEGIN
  p_id := COALESCE(NEW.product_id, OLD.product_id);
  PERFORM refresh_product_search(p_id);
  RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS t_product_tags_ins ON product_tags;
CREATE TRIGGER t_product_tags_ins
AFTER INSERT ON product_tags
FOR EACH ROW EXECUTE FUNCTION trg_product_tags_to_search();

DROP TRIGGER IF EXISTS t_product_tags_del ON product_tags;
CREATE TRIGGER t_product_tags_del
AFTER DELETE ON product_tags
FOR EACH ROW EXECUTE FUNCTION trg_product_tags_to_search();

DROP TRIGGER IF EXISTS t_product_tags_upd ON product_tags;
CREATE TRIGGER t_product_tags_upd
AFTER UPDATE OF tag_id, product_id ON product_tags
FOR EACH ROW EXECUTE FUNCTION trg_product_tags_to_search();

-- تغيّر روابط الفئات
CREATE OR REPLACE FUNCTION trg_category_product_to_search()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE p_id BIGINT;
BEGIN
  p_id := COALESCE(NEW.product_id, OLD.product_id);
  PERFORM refresh_product_search(p_id);
  RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS t_category_product_ins ON category_product;
CREATE TRIGGER t_category_product_ins
AFTER INSERT ON category_product
FOR EACH ROW EXECUTE FUNCTION trg_category_product_to_search();

DROP TRIGGER IF EXISTS t_category_product_del ON category_product;
CREATE TRIGGER t_category_product_del
AFTER DELETE ON category_product
FOR EACH ROW EXECUTE FUNCTION trg_category_product_to_search();

DROP TRIGGER IF EXISTS t_category_product_upd ON category_product;
CREATE TRIGGER t_category_product_upd
AFTER UPDATE OF category_id, product_id ON category_product
FOR EACH ROW EXECUTE FUNCTION trg_category_product_to_search();

-- تغيّر اسم التاج نفسه
CREATE OR REPLACE FUNCTION trg_tags_name_to_search()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF (TG_OP = 'UPDATE' AND NEW.name IS DISTINCT FROM OLD.name) THEN
    UPDATE products p
    SET id = p.id  -- no-op لتحفيز NOTHING؛ سنستدعي الدالة مباشرة
    WHERE EXISTS (SELECT 1 FROM product_tags pt WHERE pt.product_id = p.id AND pt.tag_id = NEW.id);
    -- تحديث مباشر:
    PERFORM refresh_product_search(p.id)
    FROM product_tags pt
    WHERE pt.product_id = p.id AND pt.tag_id = NEW.id;
  END IF;
  RETURN NULL;
END;
$$;
DROP TRIGGER IF EXISTS t_tags_upd ON tags;
CREATE TRIGGER t_tags_upd
AFTER UPDATE OF name ON tags
FOR EACH ROW EXECUTE FUNCTION trg_tags_name_to_search();

-- تغيّر اسم الفئة نفسه
CREATE OR REPLACE FUNCTION trg_categories_name_to_search()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF (TG_OP = 'UPDATE' AND NEW.name IS DISTINCT FROM OLD.name) THEN
    PERFORM refresh_product_search(p.id)
    FROM category_product cp
    JOIN products p ON p.id = cp.product_id
    WHERE cp.category_id = NEW.id;
  END IF;
  RETURN NULL;
END;
$$;
DROP TRIGGER IF EXISTS t_categories_upd ON categories;
CREATE TRIGGER t_categories_upd
AFTER UPDATE OF name ON categories
FOR EACH ROW EXECUTE FUNCTION trg_categories_name_to_search();

-- تغيّر اسم الشركة
DROP TRIGGER IF EXISTS t_companies_upd ON companies;

-- دالة تريجر: لما يتغيّر اسم app_user، وإذا كان Company، حدّث فهرسة منتجاته
CREATE OR REPLACE FUNCTION trg_app_users_name_company_to_search()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF (TG_OP = 'UPDATE' AND NEW.name IS DISTINCT FROM OLD.name) THEN
    -- نفّذ فقط إذا هذا الـ app_user هو شركة (إله صف في companies بنفس id)
    IF EXISTS (SELECT 1 FROM companies c WHERE c.id = NEW.id) THEN
      PERFORM refresh_product_search(p.id)
      FROM products p
      WHERE p.company_id = NEW.id;  -- company_id يساوي id الشركة (وهو نفسه id من app_users)
    END IF;
  END IF;
  RETURN NULL;
END;
$$;

-- فعّل التريجر على app_users(name)
DROP TRIGGER IF EXISTS t_app_users_name_upd ON app_users;
CREATE TRIGGER t_app_users_name_upd
AFTER UPDATE OF name ON app_users
FOR EACH ROW EXECUTE FUNCTION trg_app_users_name_company_to_search();

-- =====================================================================
-- 5) تهيئة أولية لكل المنتجات غير المحذوفة
-- =====================================================================
DO $$
DECLARE r RECORD;
BEGIN
  FOR r IN SELECT id FROM products WHERE (deleted IS NULL OR deleted = false)
  LOOP
    PERFORM refresh_product_search(r.id);
  END LOOP;
END$$;
