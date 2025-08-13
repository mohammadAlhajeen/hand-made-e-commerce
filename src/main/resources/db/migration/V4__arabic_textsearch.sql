-- المتطلبات
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1) إنشاء القواميس عند عدم وجودها
DO $$
BEGIN
  -- ispell العربي (يتطلب ar.aff/ar.dict في tsearch_data)
  IF NOT EXISTS (
    SELECT 1 FROM pg_ts_dict WHERE dictname = 'arabic_ispell'
  ) THEN
    CREATE TEXT SEARCH DICTIONARY arabic_ispell (
      TEMPLATE  = ispell,
      DictFile  = ar,
      AffFile   = ar,
      StopWords = ar,
  
    );
  END IF;

  -- synonym العربي (يتطلب arabic_syn.syn في tsearch_data)
  IF NOT EXISTS (
    SELECT 1 FROM pg_ts_dict WHERE dictname = 'arabic_syn'
  ) THEN
    CREATE TEXT SEARCH DICTIONARY arabic_syn (
      TEMPLATE = synonym,
      SYNONYMS = arabic_syn
    );
  END IF;
END $$;

-- 2) تأكد من وجود تكوين arabic، وإذا لم يوجد انسخه من simple
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_ts_config WHERE cfgname = 'arabic'
  ) THEN
    CREATE TEXT SEARCH CONFIGURATION arabic (COPY = simple);
  END IF;
END $$;

-- 3) ضبط الـ mapping للعربية (Idempotent)
-- الترتيب مهم: unaccent → synonym → ispell → simple (fallback)
ALTER TEXT SEARCH CONFIGURATION arabic
  ALTER MAPPING FOR asciiword, word, hword, hword_part, hword_asciipart
  WITH unaccent, arabic_syn, arabic_ispell, simple;

-- 4) دوال التطبيع/التحويل (مع تحسينات و IMMUTABLE للفهرسة)
CREATE OR REPLACE FUNCTION ar_normalize(txt text)
RETURNS text
LANGUAGE sql
IMMUTABLE
AS $$
WITH s AS (
  -- 0) lowercase + null safety
  SELECT lower(coalesce($1,'')) AS x
),
-- 1) إصلاح اللّيچاتشر: لام-ألف وأسماء الجلالة
t1 AS (
  SELECT regexp_replace(regexp_replace(x, '[ﻻﻹﻷﻵ]', 'لا', 'g'), 'ﷲ', 'الله', 'g') AS x
  FROM s
),
-- 2) توحيد الحروف (أشكال عربية/فارسية) 1-لـ-1 بدون انزياح
t2 AS (
  -- آ/أ/إ/ٱ→ا | ى/ی→ي | ﻩ/ۀ/ە→ه | ک→ك
  SELECT translate(x, 'آأإٱىیﻩۀەک', 'ااااييهههك') AS x
  FROM t1
),
t3 AS (
  -- گ→ك | ڤ→ف | چ→ج | پ→ب | ژ→ز | ڨ→ق
  SELECT translate(x, 'گڤچپژڨ', 'كفجبزق') AS x
  FROM t2
),
t4 AS (
  -- ؤ→و | ئ→ي
  SELECT translate(x, 'ؤئ', 'وي') AS x
  FROM t3
),
-- 3) توحيد الأرقام (عربية/فارسية → لاتينية)
t5 AS (
  SELECT translate(x, '٠١٢٣٤٥٦٧٨٩۰۱۲۳۴۵۶۷۸۹', '01234567890123456789') AS x
  FROM t4
),
-- 4) حذف التشكيل (064B–065F, 0670, 06D6–06ED) + الكشيدة + الرموز الخفيّة (ZWNJ/ZWJ/LRM/RLM/ALM)
t6 AS (
  SELECT regexp_replace(x, '[\u064B-\u065F\u0670\u06D6-\u06ED]|ـ|[\u200C\u200D\u200E\u200F\u061C]', '', 'g') AS x
  FROM t5
),
-- 5) طيّ المسافات
t7 AS (
  SELECT regexp_replace(x, '\s+', ' ', 'g') AS x
  FROM t6
),
-- 6) تحويل الهاء آخر الكلمة إلى تاء مربوطة (حسب طلبك)
t8 AS (
  SELECT regexp_replace(x, 'ه($|[[:space:][:punct:]،؛؟«»])', 'ة\1', 'g') AS x
  FROM t7
)
SELECT btrim(x)
FROM t8;
$$;


-- تتوقع وجود ar_normalize(text) لديك
CREATE OR REPLACE FUNCTION ar_tsvector_enriched(txt text)
RETURNS tsvector
LANGUAGE sql
IMMUTABLE
AS $$
WITH base AS (
  SELECT ar_normalize(txt) AS x
),
v AS (
  SELECT
    x,
    -- تبديل ه ⇄ ة (نولّد الاثنين)
    regexp_replace(x, '([ء-ي]+)ه(?=\b|[^ء-ي])', '\1ة', 'g') AS x_h2t,
    regexp_replace(x, '([ء-ي]+)ة(?=\b|[^ء-ي])', '\1ه', 'g') AS x_t2h,

    -- جمع المذكر السالم → مفرد: ...ون/ين → ...
    regexp_replace(x, '([ء-ي]+?)(?:ون|ين)(?=\b|[^ء-ي])', '\1', 'g') AS x_masc_sg,

    -- جمع المؤنث السالم → مفرد: ...ات → ...ة
    regexp_replace(x, '([ء-ي]+?)ات(?=\b|[^ء-ي])', '\1ة', 'g') AS x_fem_sg,

    -- المثنّى → مفرد: ...ان/ين → ...
    regexp_replace(x, '([ء-ي]+?)(?:ان|ين)(?=\b|[^ء-ي])', '\1', 'g') AS x_dual_sg
  FROM base
)
SELECT
  -- الأصل بوزن أعلى
    setweight(to_tsvector('arabic', x), 'A')
  -- التوسعات بوزن أدنى
  || setweight(to_tsvector('arabic', x_h2t),    'D')
  || setweight(to_tsvector('arabic', x_t2h),    'D')
  || setweight(to_tsvector('arabic', x_masc_sg),'D')
  || setweight(to_tsvector('arabic', x_fem_sg), 'D')
  || setweight(to_tsvector('arabic', x_dual_sg),'D')
FROM v;
$$;


CREATE OR REPLACE FUNCTION ar_tsquery(q text)
RETURNS tsquery
LANGUAGE sql
STABLE   -- ممكن تبقيها IMMUTABLE أيضًا، بس STABLE كافية للاستعلام
AS $$
  SELECT plainto_tsquery('arabic', ar_normalize(q));
$$;
