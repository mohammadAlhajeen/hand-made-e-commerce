-- (اختياري لكن مهم) تأكيد صحة مدى التقييم
-- (اختياري لكن مهم) تأكيد صحة مدى التقييم
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS reviews_rating_check;
ALTER TABLE reviews ADD CONSTRAINT reviews_rating_check CHECK (rating BETWEEN 1 AND 5);
-- =========================================================
-- 1) تنظيف قديم
-- =========================================================
DROP TRIGGER  IF EXISTS trg_reviews_after_insert ON reviews;
DROP TRIGGER  IF EXISTS trg_reviews_after_update_rating ON reviews;
DROP TRIGGER  IF EXISTS trg_reviews_after_delete ON reviews;
DROP TRIGGER  IF EXISTS trg_reviews_prevent_move ON reviews;
DROP FUNCTION IF EXISTS fn_reviews_after_insert();
DROP FUNCTION IF EXISTS fn_reviews_after_update_rating();
DROP FUNCTION IF EXISTS fn_reviews_after_delete();
DROP FUNCTION IF EXISTS prevent_product_change();
-- =========================================================
-- 2) دالة: AFTER INSERT على reviews
-- =========================================================
CREATE OR REPLACE FUNCTION fn_reviews_after_insert()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_prod BIGINT := NEW.product_id;
  v_rating INT  := NEW.rating;
  v_count BIGINT;
  v_sum   BIGINT;
  v_avg   NUMERIC(4,2);
BEGIN
  -- ضمن صف avg_rating
  INSERT INTO avg_rating(product_id) VALUES (v_prod)
  ON CONFLICT (product_id) DO NOTHING;

  UPDATE avg_rating
  SET
    one_rating   = one_rating   + CASE WHEN v_rating = 1 THEN 1 ELSE 0 END,
    two_rating   = two_rating   + CASE WHEN v_rating = 2 THEN 1 ELSE 0 END,
    three_rating = three_rating + CASE WHEN v_rating = 3 THEN 1 ELSE 0 END,
    four_rating  = four_rating  + CASE WHEN v_rating = 4 THEN 1 ELSE 0 END,
    five_rating  = five_rating  + CASE WHEN v_rating = 5 THEN 1 ELSE 0 END,
    rating_count = rating_count + 1,
    total_ratings= total_ratings+ v_rating
  WHERE product_id = v_prod;

  -- حدّث المتوسط داخل avg_rating
  SELECT rating_count, total_ratings INTO v_count, v_sum
  FROM avg_rating WHERE product_id = v_prod;

  v_avg := CASE WHEN v_count = 0 THEN 0 ELSE ROUND(v_sum::NUMERIC / v_count, 2) END;

  UPDATE avg_rating
  SET average_rating  = v_avg,
      last_recomputed = now()
  WHERE product_id = v_prod
    AND (average_rating IS DISTINCT FROM v_avg);

  RETURN NEW;
END;
$$;

-- =========================================================
-- 3) دالة: AFTER UPDATE (على rating فقط) على reviews
--    (بدون أي دعم لتغيير المنتج)
-- =========================================================
CREATE OR REPLACE FUNCTION fn_reviews_after_update_rating()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_prod BIGINT := NEW.product_id;  -- نفس المنتج
  v_old  INT    := OLD.rating;
  v_new  INT    := NEW.rating;
  v_count BIGINT;
  v_sum   BIGINT;
  v_avg   NUMERIC(4,2);
BEGIN
  IF v_old IS DISTINCT FROM v_new THEN
    -- أنقص دلو القديم
    UPDATE avg_rating
    SET
      one_rating   = GREATEST(one_rating   - CASE WHEN v_old = 1 THEN 1 ELSE 0 END, 0),
      two_rating   = GREATEST(two_rating   - CASE WHEN v_old = 2 THEN 1 ELSE 0 END, 0),
      three_rating = GREATEST(three_rating - CASE WHEN v_old = 3 THEN 1 ELSE 0 END, 0),
      four_rating  = GREATEST(four_rating  - CASE WHEN v_old = 4 THEN 1 ELSE 0 END, 0),
      five_rating  = GREATEST(five_rating  - CASE WHEN v_old = 5 THEN 1 ELSE 0 END, 0),
      total_ratings= GREATEST(total_ratings- v_old, 0)
    WHERE product_id = v_prod;

    -- زد دلو الجديد
    UPDATE avg_rating
    SET
      one_rating   = one_rating   + CASE WHEN v_new = 1 THEN 1 ELSE 0 END,
      two_rating   = two_rating   + CASE WHEN v_new = 2 THEN 1 ELSE 0 END,
      three_rating = three_rating + CASE WHEN v_new = 3 THEN 1 ELSE 0 END,
      four_rating  = four_rating  + CASE WHEN v_new = 4 THEN 1 ELSE 0 END,
      five_rating  = five_rating  + CASE WHEN v_new = 5 THEN 1 ELSE 0 END,
      total_ratings= total_ratings+ v_new
    WHERE product_id = v_prod;

    -- أعِد حساب المتوسط
    SELECT rating_count, total_ratings INTO v_count, v_sum
    FROM avg_rating WHERE product_id = v_prod;

    v_avg := CASE WHEN v_count = 0 THEN 0 ELSE ROUND(v_sum::NUMERIC / v_count, 2) END;

    UPDATE avg_rating
    SET average_rating  = v_avg,
        last_recomputed = now()
    WHERE product_id = v_prod
      AND (average_rating IS DISTINCT FROM v_avg);
  END IF;

  RETURN NEW;
END;
$$;

-- =========================================================
-- 4) منع تغيير product_id نهائيًا (لا نقل)
-- =========================================================
CREATE OR REPLACE FUNCTION prevent_product_change()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF TG_OP = 'UPDATE' AND NEW.product_id IS DISTINCT FROM OLD.product_id THEN
    RAISE EXCEPTION 'Changing product_id on reviews is not allowed';
  END IF;
  RETURN NEW;
END;
$$;

-- =========================================================
-- 5) دالة وتريجر الحذف
-- =========================================================
CREATE OR REPLACE FUNCTION fn_reviews_after_delete()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_prod BIGINT := OLD.product_id;
  v_rating INT  := OLD.rating;
  v_count BIGINT;
  v_sum   BIGINT;
  v_avg   NUMERIC(4,2);
BEGIN
  IF v_rating NOT BETWEEN 1 AND 5 THEN
    RETURN OLD;
  END IF;

  UPDATE avg_rating
  SET
    one_rating   = GREATEST(one_rating   - CASE WHEN v_rating = 1 THEN 1 ELSE 0 END, 0),
    two_rating   = GREATEST(two_rating   - CASE WHEN v_rating = 2 THEN 1 ELSE 0 END, 0),
    three_rating = GREATEST(three_rating - CASE WHEN v_rating = 3 THEN 1 ELSE 0 END, 0),
    four_rating  = GREATEST(four_rating  - CASE WHEN v_rating = 4 THEN 1 ELSE 0 END, 0),
    five_rating  = GREATEST(five_rating  - CASE WHEN v_rating = 5 THEN 1 ELSE 0 END, 0),
    rating_count = GREATEST(rating_count - 1, 0),
    total_ratings= GREATEST(total_ratings- v_rating, 0)
  WHERE product_id = v_prod;

  SELECT rating_count, total_ratings INTO v_count, v_sum
  FROM avg_rating WHERE product_id = v_prod;

  v_avg := CASE WHEN v_count = 0 THEN 0 ELSE ROUND(v_sum::NUMERIC / v_count, 2) END;

  UPDATE avg_rating
  SET average_rating  = v_avg,
      last_recomputed = now()
  WHERE product_id = v_prod
    AND (average_rating IS DISTINCT FROM v_avg);

  RETURN OLD;
END;
$$;

-- =========================================================
-- 6) إنشاء التريجرات
-- =========================================================
-- إدراج
CREATE TRIGGER trg_reviews_after_insert
AFTER INSERT ON reviews
FOR EACH ROW EXECUTE FUNCTION fn_reviews_after_insert();

-- تحديث (rating فقط)
CREATE TRIGGER trg_reviews_after_update_rating
AFTER UPDATE OF rating ON reviews
FOR EACH ROW
WHEN (OLD.rating IS DISTINCT FROM NEW.rating)
EXECUTE FUNCTION fn_reviews_after_update_rating();

-- منع نقل المراجعة لمنتج آخر
CREATE TRIGGER trg_reviews_prevent_move
BEFORE UPDATE OF product_id ON reviews
FOR EACH ROW EXECUTE FUNCTION prevent_product_change();

-- حذف
CREATE TRIGGER trg_reviews_after_delete
AFTER DELETE ON reviews
FOR EACH ROW EXECUTE FUNCTION fn_reviews_after_delete();