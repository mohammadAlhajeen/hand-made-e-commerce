# Chapter 4: Database Design and Data Management

## 4.1 Database Design Methodology

### 4.1.1 Design Approach

The database design for the Hand-Made E-Commerce System follows a systematic approach based on relational database design principles. The methodology employed includes:

1. **Requirements Analysis**: Comprehensive analysis of data requirements from stakeholder interviews and system specifications
2. **Conceptual Design**: Entity-Relationship modeling to capture business rules and relationships
3. **Logical Design**: Normalization to Third Normal Form (3NF) to ensure data integrity and minimize redundancy
4. **Physical Design**: PostgreSQL-specific optimizations including indexing strategies and performance tuning

### 4.1.2 Design Principles

- **Data Integrity**: Enforced through constraints, foreign keys, and business rules
- **Scalability**: Designed to handle growth in data volume and user base
- **Performance**: Optimized for common query patterns and transactions
- **Security**: Data protection through access controls and encryption
- **Maintainability**: Clear structure and documentation for future modifications

## 4.2 Entity-Relationship Model

### 4.2.1 Core Entities

The system's data model consists of several core entities that represent the fundamental business objects:

#### Primary Entities
1. **AppUser**: System users (customers, company owners, admins)
2. **Company**: Business entities selling products
3. **Product**: Items available for purchase
4. **Category**: Product categorization hierarchy
5. **Cart**: Shopping cart for customers
6. **Order**: Purchase transactions
7. **Attribute**: Product customization options
8. **AttributeValue**: Specific values for attributes

#### Supporting Entities
1. **Address**: Shipping and billing addresses
2. **CartItem**: Individual items in shopping carts
3. **OrderItem**: Individual items in orders
4. **ImageUrl**: Product and company images
5. **Role**: User authorization roles
6. **Status**: Various system status values

### 4.2.2 Entity Relationships

```sql
-- Core Entity Relationships Summary
-- 1:N relationships (One-to-Many)
AppUser (1) → (N) Cart
AppUser (1) → (N) Order  
Company (1) → (N) Product
Product (1) → (N) CartItem
Product (1) → (N) OrderItem
Category (1) → (N) Product

-- M:N relationships (Many-to-Many)
Product (M) ↔ (N) Attribute (via product_attributes)
CartItem (M) ↔ (N) AttributeValue (via cart_item_selections)
OrderItem (M) ↔ (N) AttributeValue (via order_item_selections)

-- 1:1 relationships (One-to-One)
Company (1) → (1) Address
AppUser (1) → (1) Address (optional)
```

## 4.3 Database Schema Design

### 4.3.1 Table Structures

#### User Management Tables

```sql
-- Users table with role-based access
CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Roles for authorization
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User-Role relationship
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES app_users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);
```

#### Company and Product Management

```sql
-- Companies/Businesses
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    cover_image_url VARCHAR(500),
    website_url VARCHAR(500),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    owner_id BIGINT NOT NULL REFERENCES app_users(id),
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT website_url_format CHECK (website_url IS NULL OR website_url ~* '^https?://.*')
);

-- Product categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES categories(id),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Products with inheritance support
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price >= 0),
    company_id BIGINT NOT NULL REFERENCES companies(id),
    category_id BIGINT REFERENCES categories(id),
    product_type VARCHAR(20) NOT NULL CHECK (product_type IN ('IN_STOCK', 'PRE_ORDER')),
    
    -- IN_STOCK specific fields
    quantity_in_stock INTEGER,
    min_stock_level INTEGER DEFAULT 0,
    
    -- PRE_ORDER specific fields
    estimated_days INTEGER,
    deposit_percentage DECIMAL(5,2) CHECK (deposit_percentage BETWEEN 0 AND 100),
    max_pre_orders INTEGER,
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints based on product type
    CONSTRAINT in_stock_fields CHECK (
        (product_type = 'IN_STOCK' AND quantity_in_stock IS NOT NULL) OR
        (product_type = 'PRE_ORDER' AND estimated_days IS NOT NULL AND deposit_percentage IS NOT NULL)
    )
);
```

#### Shopping Cart and Order Management

```sql
-- Shopping carts
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES app_users(id),
    company_id BIGINT NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(customer_id, company_id)
);

-- Cart items with selections
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    extra_price DECIMAL(10,2) DEFAULT 0 CHECK (extra_price >= 0),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    selections JSONB, -- Store attribute selections
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(cart_id, product_id, selections)
);

-- Orders
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES app_users(id),
    company_id BIGINT NOT NULL REFERENCES companies(id),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    tax_amount DECIMAL(10,2) DEFAULT 0 CHECK (tax_amount >= 0),
    shipping_cost DECIMAL(10,2) DEFAULT 0 CHECK (shipping_cost >= 0),
    discount_amount DECIMAL(10,2) DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    
    -- Deposit handling
    deposit_required BOOLEAN DEFAULT FALSE,
    deposit_amount DECIMAL(10,2) DEFAULT 0 CHECK (deposit_amount >= 0),
    deposit_paid BOOLEAN DEFAULT FALSE,
    
    shipping_address JSONB,
    billing_address JSONB,
    notes TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT deposit_logic CHECK (
        (deposit_required = FALSE AND deposit_amount = 0) OR
        (deposit_required = TRUE AND deposit_amount > 0 AND deposit_amount <= total_amount)
    )
);
```

### 4.3.2 Advanced Features

#### JSON Support for Flexible Data

```sql
-- Attribute definitions with JSON schema
CREATE TABLE attributes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'TEXT', 'NUMBER', 'SELECT', 'MULTI_SELECT', 'COLOR'
    validation_rules JSONB, -- JSON schema for validation
    display_config JSONB, -- UI configuration
    is_required BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Attribute values with pricing
CREATE TABLE attribute_values (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE,
    value_text VARCHAR(255) NOT NULL,
    extra_price DECIMAL(10,2) DEFAULT 0 CHECK (extra_price >= 0),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Product-Attribute relationships
CREATE TABLE product_attributes (
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    attribute_id BIGINT REFERENCES attributes(id) ON DELETE CASCADE,
    is_required BOOLEAN DEFAULT FALSE,
    default_value_id BIGINT REFERENCES attribute_values(id),
    PRIMARY KEY (product_id, attribute_id)
);
```

#### Full-Text Search Implementation

```sql
-- Enable full-text search extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Add search vectors to products
ALTER TABLE products ADD COLUMN search_vector tsvector;

-- Update search vector with triggers
CREATE OR REPLACE FUNCTION update_product_search_vector() 
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_product_search_vector_trigger
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_product_search_vector();
```

## 4.4 Performance Optimization

### 4.4.1 Indexing Strategy

```sql
-- Primary performance indexes
CREATE INDEX idx_products_company_category ON products(company_id, category_id) WHERE is_active = TRUE;
CREATE INDEX idx_products_search_vector ON products USING GIN(search_vector);
CREATE INDEX idx_products_price_range ON products(base_price) WHERE is_active = TRUE;
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX idx_orders_company_date ON orders(company_id, created_at DESC);
CREATE INDEX idx_cart_items_selections ON cart_items USING GIN(selections);

-- Composite indexes for common queries
CREATE INDEX idx_products_company_active_created ON products(company_id, is_active, created_at DESC);
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);
```

### 4.4.2 Query Optimization

```sql
-- Optimized query for product catalog with filters
EXPLAIN (ANALYZE, BUFFERS) 
SELECT p.id, p.name, p.base_price, p.created_at,
       c.name as company_name,
       cat.name as category_name,
       array_agg(DISTINCT iu.url) as image_urls
FROM products p
JOIN companies c ON p.company_id = c.id
LEFT JOIN categories cat ON p.category_id = cat.id
LEFT JOIN image_urls iu ON iu.product_id = p.id
WHERE p.is_active = TRUE 
  AND c.is_active = TRUE
  AND p.company_id = $1
  AND ($2 IS NULL OR p.category_id = $2)
  AND ($3 IS NULL OR p.base_price <= $3)
GROUP BY p.id, p.name, p.base_price, p.created_at, c.name, cat.name
ORDER BY p.created_at DESC
LIMIT 20 OFFSET $4;
```

## 4.5 Data Security and Privacy

### 4.5.1 Security Measures

```sql
-- Row Level Security (RLS) for multi-tenancy
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

-- Policy for company data isolation
CREATE POLICY company_products_policy ON products
    FOR ALL TO app_role
    USING (company_id IN (
        SELECT company_id FROM user_company_access 
        WHERE user_id = current_user_id()
    ));

-- Audit trail table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    operation VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE
    record_id BIGINT NOT NULL,
    old_values JSONB,
    new_values JSONB,
    user_id BIGINT REFERENCES app_users(id),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### 4.5.2 Data Encryption

```sql
-- Sensitive data encryption
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Function for email encryption
CREATE OR REPLACE FUNCTION encrypt_email(email TEXT) 
RETURNS TEXT AS $$
BEGIN
    RETURN encode(encrypt(email::bytea, 'encryption_key', 'aes'), 'base64');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

## 4.6 Database Maintenance and Monitoring

### 4.6.1 Maintenance Procedures

```sql
-- Regular maintenance tasks
-- 1. Vacuum and analyze tables
VACUUM ANALYZE products;
VACUUM ANALYZE orders;
VACUUM ANALYZE cart_items;

-- 2. Reindex for performance
REINDEX INDEX CONCURRENTLY idx_products_search_vector;

-- 3. Update table statistics
ANALYZE products;
ANALYZE orders;
```

### 4.6.2 Monitoring Queries

```sql
-- Monitor database performance
SELECT 
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats 
WHERE tablename IN ('products', 'orders', 'cart_items')
ORDER BY tablename, attname;

-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

## 4.7 Backup and Recovery Strategy

### 4.7.1 Backup Procedures

```bash
#!/bin/bash
# Daily backup script
BACKUP_DIR="/var/backups/handmade_ecommerce"
DATE=$(date +%Y%m%d_%H%M%S)

# Full database backup
pg_dump -h localhost -U app_user -d handmade_ecommerce \
    --verbose --format=custom \
    --file="$BACKUP_DIR/full_backup_$DATE.dump"

# Schema-only backup
pg_dump -h localhost -U app_user -d handmade_ecommerce \
    --schema-only --verbose \
    --file="$BACKUP_DIR/schema_backup_$DATE.sql"
```

### 4.7.2 Recovery Procedures

```bash
#!/bin/bash
# Recovery script
BACKUP_FILE="$1"

# Restore database
pg_restore -h localhost -U app_user -d handmade_ecommerce \
    --verbose --clean --if-exists \
    "$BACKUP_FILE"

# Verify restoration
psql -h localhost -U app_user -d handmade_ecommerce \
    -c "SELECT COUNT(*) FROM products; SELECT COUNT(*) FROM orders;"
```
