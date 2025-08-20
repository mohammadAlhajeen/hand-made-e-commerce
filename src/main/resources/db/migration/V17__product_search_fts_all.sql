-- V17: create and maintain product_search.fts_all tsvector
-- Adds fts_all column, trigger function to compute it, backfill and GIN index.

ALTER TABLE product_search
  ADD COLUMN IF NOT EXISTS fts_all tsvector;

CREATE OR REPLACE FUNCTION product_search_compute_fts_all()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  -- assume component columns are already tsvector; coalesce to empty tsvector when null
  NEW.fts_all :=
    COALESCE(NEW.fts_name, ''::tsvector)
    || COALESCE(NEW.fts_tags, ''::tsvector)
    || COALESCE(NEW.fts_company, ''::tsvector)
    || COALESCE(NEW.fts_categories, ''::tsvector)
    || COALESCE(NEW.fts_description, ''::tsvector);
  RETURN NEW;
END;
$$;

-- trigger to keep fts_all up-to-date
DROP TRIGGER IF EXISTS trg_product_search_fts_all_upd ON product_search;
CREATE TRIGGER trg_product_search_fts_all_upd
BEFORE INSERT OR UPDATE ON product_search
FOR EACH ROW EXECUTE FUNCTION product_search_compute_fts_all();

-- backfill existing rows
UPDATE product_search SET fts_all =
  COALESCE(fts_name, ''::tsvector)
  || COALESCE(fts_tags, ''::tsvector)
  || COALESCE(fts_company, ''::tsvector)
  || COALESCE(fts_categories, ''::tsvector)
  || COALESCE(fts_description, ''::tsvector)
WHERE fts_all IS NULL;

-- index
CREATE INDEX IF NOT EXISTS idx_product_search_fts_all
  ON product_search USING gin(fts_all);

-- end
