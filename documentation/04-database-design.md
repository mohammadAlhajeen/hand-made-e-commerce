# Detailed Database Report
## Hand-Made E-Commerce Database Design

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Database Architecture](#database-architecture)
3. [Table Design](#table-design)
4. [Relations and Indexes](#relations-and-indexes)
5. [Optimized Queries](#optimized-queries)
6. [Security Procedures](#security-procedures)
7. [Backup and Recovery](#backup-and-recovery)
8. [Performance Monitoring](#performance-monitoring)

---

## Database Overview

### Database Information
- **Database Type**: PostgreSQL 15.x
- **Database Name**: `handmade_ecommerce`
- **Encoding**: UTF-8
- **Timezone**: UTC
- **Expected Database Size**: 50-100 GB
- **Number of Tables**: 25+ tables

### Technical Requirements
```sql
-- Initial database setup
CREATE DATABASE handmade_ecommerce
    WITH 
    OWNER = app_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
```

### Distinguished Features
1. **JSONB Support**: For flexible data storage
2. **Full-Text Search**: For advanced text searching
3. **UUID Support**: For unique identifiers
4. **Audit Trails**: لتتبع التغييرات
5. **Partitioning**: لتحسين الأداء

---

## معمارية قاعدة البيانات

### تقسيم الأسكيمات

```sql
-- إنشاء الأسكيمات المختلفة
CREATE SCHEMA IF NOT EXISTS user_management;
CREATE SCHEMA IF NOT EXISTS product_catalog;
CREATE SCHEMA IF NOT EXISTS order_processing;
CREATE SCHEMA IF NOT EXISTS content_management;
CREATE SCHEMA IF NOT EXISTS audit_logs;
CREATE SCHEMA IF NOT EXISTS system_config;
```

### توزيع الجداول حسب الأسكيما

#### 1. User Management Schema
- `app_users` - الجدول الأساسي للمستخدمين
- `companies` - بيانات الشركات
- `customers` - بيانات العملاء
- `drivers` - بيانات السائقين
- `user_roles` - أدوار المستخدمين
- `user_sessions` - جلسات المستخدمين

#### 2. Product Catalog Schema
- `categories` - التصنيفات
- `products` - المنتجات الأساسية
- `in_stock_products` - المنتجات الجاهزة
- `pre_order_products` - منتجات الطلب المسبق
- `attributes` - سمات المنتجات
- `attribute_values` - قيم السمات
- `product_images` - صور المنتجات
- `reviews` - المراجعات والتقييمات
- `tags` - العلامات

#### 3. Order Processing Schema
- `carts` - السلال
- `cart_items` - عناصر السلة
- `cart_item_selections` - اختيارات السمات في السلة
- `orders` - الطلبات
- `order_items` - عناصر الطلبات
- `order_item_selections` - اختيارات السمات في الطلبات
- `shipments` - الشحنات
- `shipment_items` - عناصر الشحنات
- `payments` - المدفوعات

#### 4. Content Management Schema
- `company_pages` - صفحات الشركات
- `page_sections` - أقسام الصفحات
- `media_items` - ملفات الوسائط
- `navigation_links` - روابط التنقل
- `social_links` - الروابط الاجتماعية

---

## تصميم الجداول

### 1. جداول إدارة المستخدمين

#### جدول app_users
```sql
CREATE TABLE user_management.app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(20),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    user_type VARCHAR(50) NOT NULL CHECK (user_type IN ('COMPANY', 'CUSTOMER', 'DRIVER', 'ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- فهارس الأداء
CREATE INDEX idx_app_users_email ON user_management.app_users(email);
CREATE INDEX idx_app_users_username ON user_management.app_users(username);
CREATE INDEX idx_app_users_type ON user_management.app_users(user_type);
CREATE INDEX idx_app_users_active ON user_management.app_users(is_active) WHERE is_active = true;

-- فهرس البحث النصي
CREATE INDEX idx_app_users_search ON user_management.app_users 
USING gin(to_tsvector('english', first_name || ' ' || last_name || ' ' || COALESCE(email, '')));
```

#### جدول companies
```sql
CREATE TABLE user_management.companies (
    id BIGINT PRIMARY KEY REFERENCES user_management.app_users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    commercial_name VARCHAR(255),
    description TEXT,
    business_license VARCHAR(255),
    tax_number VARCHAR(255),
    registration_number VARCHAR(255),
    industry_type VARCHAR(100),
    website_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP WITH TIME ZONE,
    verification_notes TEXT,
    business_address JSONB,
    business_hours JSONB,
    payment_info JSONB,
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- فهارس للبحث
CREATE INDEX idx_companies_name ON user_management.companies(company_name);
CREATE INDEX idx_companies_verified ON user_management.companies(is_verified);
CREATE INDEX idx_companies_industry ON user_management.companies(industry_type);

-- فهرس JSONB للعنوان
CREATE INDEX idx_companies_address_gin ON user_management.companies USING gin(business_address);
```

#### جدول customers
```sql
CREATE TABLE user_management.customers (
    id BIGINT PRIMARY KEY REFERENCES user_management.app_users(id) ON DELETE CASCADE,
    birth_date DATE,
    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    preferences JSONB DEFAULT '{}',
    loyalty_points INTEGER DEFAULT 0,
    total_orders INTEGER DEFAULT 0,
    total_spent DECIMAL(12,2) DEFAULT 0.00,
    favorite_categories INTEGER[],
    notification_preferences JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_birth_date ON user_management.customers(birth_date);
CREATE INDEX idx_customers_loyalty_points ON user_management.customers(loyalty_points);
```

### 2. جداول كتالوج المنتجات

#### جدول categories
```sql
CREATE TABLE product_catalog.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES product_catalog.categories(id),
    level INTEGER DEFAULT 0,
    path LTREE, -- PostgreSQL LTREE extension for hierarchical data
    icon_url VARCHAR(500),
    banner_image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    order_index INTEGER DEFAULT 0,
    seo_title VARCHAR(255),
    seo_description TEXT,
    seo_keywords TEXT[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- فهارس للهيكل الهرمي
CREATE INDEX idx_categories_parent ON product_catalog.categories(parent_id);
CREATE INDEX idx_categories_path ON product_catalog.categories USING gist(path);
CREATE INDEX idx_categories_slug ON product_catalog.categories(slug);
CREATE INDEX idx_categories_active ON product_catalog.categories(is_active) WHERE is_active = true;
```

#### جدول products (الجدول الأساسي)
```sql
CREATE TABLE product_catalog.products (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES user_management.companies(id),
    sku VARCHAR(100) UNIQUE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    description TEXT,
    short_description VARCHAR(500),
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price >= 0),
    compare_price DECIMAL(10,2),
    cost_price DECIMAL(10,2),
    category_id BIGINT REFERENCES product_catalog.categories(id),
    brand VARCHAR(255),
    weight DECIMAL(8,3),
    dimensions JSONB, -- {length, width, height, unit}
    materials TEXT[],
    care_instructions TEXT,
    product_type VARCHAR(50) NOT NULL CHECK (product_type IN ('IN_STOCK', 'PRE_ORDER')),
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'DISCONTINUED')),
    is_featured BOOLEAN DEFAULT FALSE,
    requires_shipping BOOLEAN DEFAULT TRUE,
    tax_class VARCHAR(50) DEFAULT 'STANDARD',
    seo_title VARCHAR(255),
    seo_description TEXT,
    search_keywords TEXT[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint per company
    UNIQUE(company_id, slug)
);

-- فهارس الأداء المحسنة
CREATE INDEX idx_products_company ON product_catalog.products(company_id);
CREATE INDEX idx_products_category ON product_catalog.products(category_id);
CREATE INDEX idx_products_type ON product_catalog.products(product_type);
CREATE INDEX idx_products_status ON product_catalog.products(status);
CREATE INDEX idx_products_featured ON product_catalog.products(is_featured) WHERE is_featured = true;
CREATE INDEX idx_products_price ON product_catalog.products(base_price);
CREATE INDEX idx_products_created ON product_catalog.products(created_at DESC);

-- فهرس البحث النصي المتقدم
CREATE INDEX idx_products_search ON product_catalog.products 
USING gin(to_tsvector('english', name || ' ' || COALESCE(description, '') || ' ' || COALESCE(brand, '')));

-- فهرس للأبعاد JSONB
CREATE INDEX idx_products_dimensions ON product_catalog.products USING gin(dimensions);
```

#### جدول in_stock_products
```sql
CREATE TABLE product_catalog.in_stock_products (
    id BIGINT PRIMARY KEY REFERENCES product_catalog.products(id) ON DELETE CASCADE,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
    reserved_quantity INTEGER DEFAULT 0 CHECK (reserved_quantity >= 0),
    min_stock_level INTEGER DEFAULT 0,
    max_stock_level INTEGER,
    reorder_point INTEGER DEFAULT 0,
    track_inventory BOOLEAN DEFAULT TRUE,
    allow_backorders BOOLEAN DEFAULT FALSE,
    stock_status VARCHAR(20) DEFAULT 'IN_STOCK' CHECK (stock_status IN ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'DISCONTINUED')),
    last_stock_update TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_in_stock_quantity ON product_catalog.in_stock_products(quantity_in_stock);
CREATE INDEX idx_in_stock_status ON product_catalog.in_stock_products(stock_status);
CREATE INDEX idx_in_stock_low ON product_catalog.in_stock_products(quantity_in_stock, min_stock_level) 
WHERE quantity_in_stock <= min_stock_level;
```

#### جدول pre_order_products
```sql
CREATE TABLE product_catalog.pre_order_products (
    id BIGINT PRIMARY KEY REFERENCES product_catalog.products(id) ON DELETE CASCADE,
    estimated_days INTEGER CHECK (estimated_days > 0),
    min_production_time INTEGER DEFAULT 1,
    max_production_time INTEGER,
    deposit_percentage DECIMAL(5,2) DEFAULT 30.00 CHECK (deposit_percentage >= 0 AND deposit_percentage <= 100),
    deposit_amount DECIMAL(10,2),
    max_pre_orders INTEGER,
    current_pre_orders INTEGER DEFAULT 0,
    production_capacity_per_month INTEGER,
    requires_custom_work BOOLEAN DEFAULT FALSE,
    custom_work_fields JSONB, -- للحقول المخصصة
    pre_order_start_date TIMESTAMP WITH TIME ZONE,
    pre_order_end_date TIMESTAMP WITH TIME ZONE,
    next_production_batch DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pre_order_capacity ON product_catalog.pre_order_products(current_pre_orders, max_pre_orders);
CREATE INDEX idx_pre_order_dates ON product_catalog.pre_order_products(pre_order_start_date, pre_order_end_date);
```

#### جدول attributes
```sql
CREATE TABLE product_catalog.attributes (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product_catalog.products(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('TEXT', 'NUMBER', 'SELECT', 'MULTI_SELECT', 'BOOLEAN', 'COLOR', 'SIZE')),
    required BOOLEAN DEFAULT FALSE,
    order_index INTEGER DEFAULT 0,
    validation_rules JSONB, -- قواعد التحقق
    display_options JSONB, -- خيارات العرض
    help_text TEXT,
    is_variant BOOLEAN DEFAULT FALSE, -- هل يؤثر على السعر/المخزون
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attributes_product ON product_catalog.attributes(product_id);
CREATE INDEX idx_attributes_type ON product_catalog.attributes(type);
CREATE INDEX idx_attributes_order ON product_catalog.attributes(order_index);
```

#### جدول attribute_values
```sql
CREATE TABLE product_catalog.attribute_values (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL REFERENCES product_catalog.attributes(id) ON DELETE CASCADE,
    value VARCHAR(255) NOT NULL,
    display_value VARCHAR(255), -- للعرض المترجم
    extra_price DECIMAL(10,2) DEFAULT 0.00,
    color_code VARCHAR(7), -- للألوان HEX
    image_url VARCHAR(500),
    is_default BOOLEAN DEFAULT FALSE,
    order_index INTEGER DEFAULT 0,
    is_available BOOLEAN DEFAULT TRUE,
    stock_quantity INTEGER, -- للمتغيرات التي تؤثر على المخزون
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(attribute_id, value)
);

CREATE INDEX idx_attr_values_attribute ON product_catalog.attribute_values(attribute_id);
CREATE INDEX idx_attr_values_price ON product_catalog.attribute_values(extra_price);
CREATE INDEX idx_attr_values_available ON product_catalog.attribute_values(is_available) WHERE is_available = true;
```

### 3. جداول معالجة الطلبات

#### جدول carts
```sql
CREATE TABLE order_processing.carts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES user_management.customers(id),
    company_id BIGINT NOT NULL REFERENCES user_management.companies(id),
    session_id VARCHAR(255), -- للضيوف
    currency VARCHAR(3) DEFAULT 'ILS',
    notes TEXT,
    coupon_code VARCHAR(50),
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    shipping_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    expires_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 days'),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(customer_id, company_id)
);

CREATE INDEX idx_carts_customer ON order_processing.carts(customer_id);
CREATE INDEX idx_carts_company ON order_processing.carts(company_id);
CREATE INDEX idx_carts_session ON order_processing.carts(session_id);
CREATE INDEX idx_carts_expires ON order_processing.carts(expires_at);
```

#### جدول cart_items
```sql
CREATE TABLE order_processing.cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES order_processing.carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product_catalog.products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    extra_price DECIMAL(10,2) DEFAULT 0.00,
    total_price DECIMAL(10,2) GENERATED ALWAYS AS ((unit_price + extra_price) * quantity) STORED,
    deposit_required BOOLEAN DEFAULT FALSE,
    deposit_amount DECIMAL(10,2) DEFAULT 0.00,
    deposit_percentage DECIMAL(5,2),
    remaining_amount DECIMAL(10,2) GENERATED ALWAYS AS (total_price - COALESCE(deposit_amount, 0)) STORED,
    custom_data JSONB, -- للبيانات المخصصة
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cart_items_cart ON order_processing.cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON order_processing.cart_items(product_id);
CREATE INDEX idx_cart_items_deposit ON order_processing.cart_items(deposit_required) WHERE deposit_required = true;
```

#### جدول orders
```sql
CREATE TABLE order_processing.orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES user_management.customers(id),
    company_id BIGINT NOT NULL REFERENCES user_management.companies(id),
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN (
        'PENDING', 'PENDING_DEPOSIT', 'DEPOSIT_PAID', 'CONFIRMED', 
        'PROCESSING', 'PARTIALLY_SHIPPED', 'SHIPPED', 'DELIVERED', 
        'CANCELLED', 'REFUNDED'
    )),
    order_type VARCHAR(20) DEFAULT 'STANDARD' CHECK (order_type IN ('STANDARD', 'PRE_ORDER', 'MIXED')),
    
    -- المبالغ المالية
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    shipping_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL,
    
    -- العربون والدفع
    deposit_amount DECIMAL(10,2) DEFAULT 0.00,
    deposit_paid DECIMAL(10,2) DEFAULT 0.00,
    remaining_amount DECIMAL(12,2) GENERATED ALWAYS AS (total_amount - COALESCE(deposit_paid, 0)) STORED,
    final_payment_due_date DATE,
    
    -- عنوان الشحن
    shipping_address JSONB NOT NULL,
    billing_address JSONB,
    
    -- تواريخ مهمة
    estimated_completion_date DATE,
    estimated_shipping_date DATE,
    estimated_delivery_date DATE,
    completed_at TIMESTAMP WITH TIME ZONE,
    shipped_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    
    -- معلومات إضافية
    currency VARCHAR(3) DEFAULT 'ILS',
    notes TEXT,
    internal_notes TEXT, -- ملاحظات داخلية للشركة
    cancellation_reason TEXT,
    coupon_code VARCHAR(50),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- فهارس متقدمة للأداء
CREATE INDEX idx_orders_customer ON order_processing.orders(customer_id);
CREATE INDEX idx_orders_company ON order_processing.orders(company_id);
CREATE INDEX idx_orders_status ON order_processing.orders(status);
CREATE INDEX idx_orders_number ON order_processing.orders(order_number);
CREATE INDEX idx_orders_created ON order_processing.orders(created_at DESC);
CREATE INDEX idx_orders_total ON order_processing.orders(total_amount DESC);
CREATE INDEX idx_orders_pending_deposit ON order_processing.orders(status) WHERE status = 'PENDING_DEPOSIT';

-- فهرس مركب للتقارير
CREATE INDEX idx_orders_company_status_date ON order_processing.orders(company_id, status, created_at DESC);
```

### 4. جداول إدارة المحتوى

#### جدول media_items
```sql
CREATE TABLE content_management.media_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    uploaded_by BIGINT REFERENCES user_management.app_users(id),
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255),
    file_path VARCHAR(1000) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    width INTEGER,
    height INTEGER,
    duration INTEGER, -- للفيديو بالثواني
    alt_text TEXT,
    caption TEXT,
    tags TEXT[],
    metadata JSONB,
    is_public BOOLEAN DEFAULT TRUE,
    is_processed BOOLEAN DEFAULT FALSE,
    processing_status VARCHAR(20) DEFAULT 'PENDING',
    thumbnail_url VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_uploaded_by ON content_management.media_items(uploaded_by);
CREATE INDEX idx_media_content_type ON content_management.media_items(content_type);
CREATE INDEX idx_media_tags ON content_management.media_items USING gin(tags);
CREATE INDEX idx_media_public ON content_management.media_items(is_public) WHERE is_public = true;
```

---

## العلاقات والفهارس

### 1. العلاقات الأساسية

#### One-to-Many Relationships
```sql
-- شركة واحدة لها عدة منتجات
ALTER TABLE product_catalog.products 
ADD CONSTRAINT fk_products_company 
FOREIGN KEY (company_id) REFERENCES user_management.companies(id);

-- منتج واحد له عدة صور
CREATE TABLE product_catalog.product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id BIGINT NOT NULL REFERENCES product_catalog.products(id) ON DELETE CASCADE,
    media_item_id UUID NOT NULL REFERENCES content_management.media_items(id),
    is_main BOOLEAN DEFAULT FALSE,
    order_index INTEGER DEFAULT 0,
    alt_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

#### Many-to-Many Relationships
```sql
-- المنتجات والعلامات
CREATE TABLE product_catalog.product_tags (
    product_id BIGINT REFERENCES product_catalog.products(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES product_catalog.tags(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, tag_id)
);

-- المستخدمين والأدوار
CREATE TABLE user_management.user_roles (
    user_id BIGINT REFERENCES user_management.app_users(id) ON DELETE CASCADE,
    role_name VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT REFERENCES user_management.app_users(id),
    PRIMARY KEY (user_id, role_name)
);
```

### 2. فهارس الأداء المتقدمة

#### فهارس مركبة للاستعلامات الشائعة
```sql
-- للبحث في المنتجات حسب الشركة والتصنيف
CREATE INDEX idx_products_company_category ON product_catalog.products(company_id, category_id);

-- للبحث في المنتجات حسب الحالة والسعر
CREATE INDEX idx_products_status_price ON product_catalog.products(status, base_price) 
WHERE status = 'ACTIVE';

-- للطلبات حسب العميل والحالة
CREATE INDEX idx_orders_customer_status ON order_processing.orders(customer_id, status);

-- للمنتجات المميزة النشطة
CREATE INDEX idx_products_featured_active ON product_catalog.products(is_featured, status) 
WHERE is_featured = true AND status = 'ACTIVE';
```

#### فهارس للبحث النصي
```sql
-- فهرس البحث النصي للمنتجات
CREATE INDEX idx_products_fulltext ON product_catalog.products 
USING gin(to_tsvector('english', name || ' ' || COALESCE(description, '') || ' ' || COALESCE(brand, '')));

-- فهرس البحث للشركات
CREATE INDEX idx_companies_fulltext ON user_management.companies 
USING gin(to_tsvector('english', company_name || ' ' || COALESCE(description, '')));
```

#### فهارس JSONB
```sql
-- فهرس لعنوان الشحن في الطلبات
CREATE INDEX idx_orders_shipping_city ON order_processing.orders 
USING gin((shipping_address->>'city'));

-- فهرس لتفضيلات العملاء
CREATE INDEX idx_customers_preferences ON user_management.customers 
USING gin(preferences);
```

### 3. القيود والتحقق

#### قيود التحقق من البيانات
```sql
-- التأكد من صحة السعر
ALTER TABLE product_catalog.products 
ADD CONSTRAINT chk_valid_prices 
CHECK (base_price >= 0 AND (compare_price IS NULL OR compare_price >= base_price));

-- التأكد من صحة الكمية
ALTER TABLE order_processing.cart_items 
ADD CONSTRAINT chk_positive_quantity 
CHECK (quantity > 0);

-- التأكد من صحة النسبة المئوية للعربون
ALTER TABLE product_catalog.pre_order_products 
ADD CONSTRAINT chk_deposit_percentage 
CHECK (deposit_percentage >= 0 AND deposit_percentage <= 100);
```

#### قيود الفرادة المركبة
```sql
-- عدم تكرار المنتج في نفس السلة
ALTER TABLE order_processing.cart_items 
ADD CONSTRAINT uk_cart_product 
UNIQUE (cart_id, product_id);

-- فرادة اسم الشركة لكل مستخدم
ALTER TABLE user_management.companies 
ADD CONSTRAINT uk_company_name 
UNIQUE (company_name);
```

---

## استعلامات محسنة

### 1. استعلامات البحث والفلترة

#### البحث في المنتجات مع الفلترة
```sql
-- استعلام محسن للبحث في المنتجات
WITH product_search AS (
    SELECT 
        p.*,
        c.name as category_name,
        comp.company_name,
        COALESCE(avg_rating.rating, 0) as avg_rating,
        COALESCE(review_count.count, 0) as review_count,
        main_image.url as main_image_url,
        CASE 
            WHEN p.product_type = 'IN_STOCK' THEN isp.quantity_in_stock > 0
            WHEN p.product_type = 'PRE_ORDER' THEN pop.current_pre_orders < pop.max_pre_orders
        END as is_available,
        ts_rank(
            to_tsvector('english', p.name || ' ' || COALESCE(p.description, '')),
            plainto_tsquery('english', $1)
        ) as search_rank
    FROM product_catalog.products p
    LEFT JOIN product_catalog.categories c ON p.category_id = c.id
    LEFT JOIN user_management.companies comp ON p.company_id = comp.id
    LEFT JOIN product_catalog.in_stock_products isp ON p.id = isp.id
    LEFT JOIN product_catalog.pre_order_products pop ON p.id = pop.id
    LEFT JOIN (
        SELECT product_id, AVG(rating) as rating
        FROM product_catalog.reviews
        WHERE is_approved = true
        GROUP BY product_id
    ) avg_rating ON p.id = avg_rating.product_id
    LEFT JOIN (
        SELECT product_id, COUNT(*) as count
        FROM product_catalog.reviews
        WHERE is_approved = true
        GROUP BY product_id
    ) review_count ON p.id = review_count.product_id
    LEFT JOIN (
        SELECT DISTINCT ON (product_id) product_id, mi.url
        FROM product_catalog.product_images pi
        JOIN content_management.media_items mi ON pi.media_item_id = mi.id
        WHERE pi.is_main = true
        ORDER BY product_id, pi.order_index
    ) main_image ON p.id = main_image.product_id
    WHERE 
        p.status = 'ACTIVE'
        AND ($1 IS NULL OR to_tsvector('english', p.name || ' ' || COALESCE(p.description, '')) @@ plainto_tsquery('english', $1))
        AND ($2 IS NULL OR c.slug = $2)
        AND ($3 IS NULL OR p.base_price >= $3)
        AND ($4 IS NULL OR p.base_price <= $4)
        AND ($5 IS NULL OR comp.id = $5)
)
SELECT *
FROM product_search
WHERE ($6 IS NULL OR $6 = false OR is_available = true)
ORDER BY 
    CASE WHEN $7 = 'price_asc' THEN base_price END ASC,
    CASE WHEN $7 = 'price_desc' THEN base_price END DESC,
    CASE WHEN $7 = 'rating_desc' THEN avg_rating END DESC,
    CASE WHEN $7 = 'newest' THEN created_at END DESC,
    search_rank DESC,
    created_at DESC
LIMIT $8 OFFSET $9;
```

#### استعلام السلة مع التفاصيل
```sql
-- استعلام شامل لعرض محتويات السلة
SELECT 
    c.id as cart_id,
    c.company_id,
    comp.company_name,
    json_agg(
        json_build_object(
            'item_id', ci.id,
            'product_id', p.id,
            'product_name', p.name,
            'product_type', p.product_type,
            'quantity', ci.quantity,
            'unit_price', ci.unit_price,
            'extra_price', ci.extra_price,
            'total_price', ci.total_price,
            'deposit_required', ci.deposit_required,
            'deposit_amount', ci.deposit_amount,
            'remaining_amount', ci.remaining_amount,
            'main_image_url', mi.url,
            'selections', ci_selections.selections
        ) ORDER BY ci.created_at
    ) as items,
    json_build_object(
        'total_items', SUM(ci.quantity),
        'subtotal', SUM(ci.total_price),
        'total_deposit', SUM(COALESCE(ci.deposit_amount, 0)),
        'grand_total', SUM(ci.total_price)
    ) as summary
FROM order_processing.carts c
JOIN user_management.companies comp ON c.company_id = comp.id
LEFT JOIN order_processing.cart_items ci ON c.id = ci.cart_id
LEFT JOIN product_catalog.products p ON ci.product_id = p.id
LEFT JOIN (
    SELECT DISTINCT ON (pi.product_id) pi.product_id, mi.url
    FROM product_catalog.product_images pi
    JOIN content_management.media_items mi ON pi.media_item_id = mi.id
    WHERE pi.is_main = true
    ORDER BY pi.product_id, pi.order_index
) mi ON p.id = mi.product_id
LEFT JOIN (
    SELECT 
        cis.cart_item_id,
        json_agg(
            json_build_object(
                'attribute_id', a.id,
                'attribute_name', a.name,
                'selected_values', (
                    SELECT json_agg(
                        json_build_object(
                            'value_id', av.id,
                            'value', av.value,
                            'extra_price', av.extra_price
                        )
                    )
                    FROM jsonb_array_elements_text(cis.selected_value_ids) vid
                    JOIN product_catalog.attribute_values av ON av.id = vid::bigint
                )
            )
        ) as selections
    FROM order_processing.cart_item_selections cis
    JOIN product_catalog.attributes a ON cis.attribute_id = a.id
    GROUP BY cis.cart_item_id
) ci_selections ON ci.id = ci_selections.cart_item_id
WHERE c.customer_id = $1 AND c.company_id = $2
GROUP BY c.id, c.company_id, comp.company_name;
```

### 2. استعلامات التقارير

#### تقرير مبيعات الشركة
```sql
-- تقرير مبيعات شهري للشركة
WITH monthly_sales AS (
    SELECT 
        DATE_TRUNC('month', o.created_at) as month,
        COUNT(*) as total_orders,
        SUM(o.total_amount) as total_revenue,
        SUM(o.deposit_paid) as total_deposits,
        AVG(o.total_amount) as avg_order_value,
        COUNT(DISTINCT o.customer_id) as unique_customers
    FROM order_processing.orders o
    WHERE 
        o.company_id = $1
        AND o.status NOT IN ('CANCELLED', 'REFUNDED')
        AND o.created_at >= $2
        AND o.created_at <= $3
    GROUP BY DATE_TRUNC('month', o.created_at)
),
product_performance AS (
    SELECT 
        p.id,
        p.name,
        SUM(oi.quantity) as total_sold,
        SUM(oi.total_price) as total_revenue,
        COUNT(DISTINCT oi.order_id) as orders_count
    FROM order_processing.order_items oi
    JOIN product_catalog.products p ON oi.product_id = p.id
    JOIN order_processing.orders o ON oi.order_id = o.id
    WHERE 
        p.company_id = $1
        AND o.status NOT IN ('CANCELLED', 'REFUNDED')
        AND o.created_at >= $2
        AND o.created_at <= $3
    GROUP BY p.id, p.name
    ORDER BY total_revenue DESC
    LIMIT 10
)
SELECT 
    json_build_object(
        'monthly_sales', (SELECT json_agg(row_to_json(ms)) FROM monthly_sales ms),
        'top_products', (SELECT json_agg(row_to_json(pp)) FROM product_performance pp),
        'summary', json_build_object(
            'total_revenue', (SELECT SUM(total_revenue) FROM monthly_sales),
            'total_orders', (SELECT SUM(total_orders) FROM monthly_sales),
            'avg_monthly_revenue', (SELECT AVG(total_revenue) FROM monthly_sales)
        )
    ) as report;
```

---

## إجراءات الأمان

### 1. أمان المستخدمين

#### تشفير كلمات المرور
```sql
-- تطبيق سياسة كلمات المرور القوية
CREATE OR REPLACE FUNCTION validate_password(password TEXT) 
RETURNS BOOLEAN AS $$
BEGIN
    RETURN (
        LENGTH(password) >= 8 AND
        password ~ '[A-Z]' AND  -- حرف كبير
        password ~ '[a-z]' AND  -- حرف صغير
        password ~ '[0-9]' AND  -- رقم
        password ~ '[!@#$%^&*(),.?":{}|<>]' -- رمز خاص
    );
END;
$$ LANGUAGE plpgsql;

-- تطبيق القيد على جدول المستخدمين
ALTER TABLE user_management.app_users 
ADD CONSTRAINT chk_strong_password 
CHECK (validate_password(password));
```

#### تتبع محاولات تسجيل الدخول
```sql
CREATE TABLE user_management.login_attempts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    success BOOLEAN,
    failure_reason VARCHAR(100),
    attempted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_attempts_username ON user_management.login_attempts(username);
CREATE INDEX idx_login_attempts_ip ON user_management.login_attempts(ip_address);
CREATE INDEX idx_login_attempts_time ON user_management.login_attempts(attempted_at);
```

### 2. Row Level Security (RLS)

#### حماية بيانات الشركات
```sql
-- تمكين RLS على جدول المنتجات
ALTER TABLE product_catalog.products ENABLE ROW LEVEL SECURITY;

-- سياسة للشركات: يمكن للشركة رؤية منتجاتها فقط
CREATE POLICY products_company_policy ON product_catalog.products
    FOR ALL TO company_role
    USING (company_id = current_setting('app.current_company_id')::bigint);

-- سياسة للعملاء: يمكن رؤية المنتجات النشطة فقط
CREATE POLICY products_public_policy ON product_catalog.products
    FOR SELECT TO public
    USING (status = 'ACTIVE');
```

#### حماية بيانات الطلبات
```sql
ALTER TABLE order_processing.orders ENABLE ROW LEVEL SECURITY;

-- الشركة ترى طلباتها فقط
CREATE POLICY orders_company_policy ON order_processing.orders
    FOR ALL TO company_role
    USING (company_id = current_setting('app.current_company_id')::bigint);

-- العميل يرى طلباته فقط
CREATE POLICY orders_customer_policy ON order_processing.orders
    FOR ALL TO customer_role
    USING (customer_id = current_setting('app.current_user_id')::bigint);
```

### 3. تشفير البيانات الحساسة

#### تشفير معلومات الدفع
```sql
-- إنشاء مفتاح تشفير
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- جدول معلومات الدفع المشفرة
CREATE TABLE order_processing.payment_methods (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES user_management.customers(id),
    method_type VARCHAR(20) NOT NULL,
    encrypted_data BYTEA NOT NULL, -- بيانات مشفرة
    mask VARCHAR(20), -- 4 أرقام أخيرة مثلاً
    is_default BOOLEAN DEFAULT FALSE,
    expires_at DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- دالة للتشفير
CREATE OR REPLACE FUNCTION encrypt_payment_data(data TEXT, key TEXT)
RETURNS BYTEA AS $$
BEGIN
    RETURN pgp_sym_encrypt(data, key);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- دالة لفك التشفير
CREATE OR REPLACE FUNCTION decrypt_payment_data(encrypted_data BYTEA, key TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN pgp_sym_decrypt(encrypted_data, key);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

## النسخ الاحتياطي والاستعادة

### 1. استراتيجية النسخ الاحتياطي

#### النسخ الاحتياطي الكامل اليومي
```bash
#!/bin/bash
# daily_backup.sh

DB_NAME="handmade_ecommerce"
BACKUP_DIR="/backups/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)

# نسخ احتياطي كامل
pg_dump -h localhost -U postgres -d $DB_NAME \
    --format=custom \
    --compress=9 \
    --verbose \
    --file="$BACKUP_DIR/full_backup_$DATE.dump"

# ضغط إضافي
gzip "$BACKUP_DIR/full_backup_$DATE.dump"

# حذف النسخ القديمة (أكثر من 30 يوم)
find $BACKUP_DIR -name "full_backup_*.dump.gz" -mtime +30 -delete

echo "Backup completed: full_backup_$DATE.dump.gz"
```

#### النسخ الاحتياطي التزايدي
```bash
#!/bin/bash
# incremental_backup.sh

# تمكين WAL archiving في postgresql.conf
# wal_level = replica
# archive_mode = on
# archive_command = 'cp %p /backups/postgresql/wal/%f'

WAL_DIR="/backups/postgresql/wal"
BASE_BACKUP_DIR="/backups/postgresql/base"

# إنشاء base backup
pg_basebackup -h localhost -U postgres \
    --pgdata="$BASE_BACKUP_DIR/$(date +%Y%m%d)" \
    --format=tar \
    --gzip \
    --progress \
    --verbose
```

### 2. إجراءات الاستعادة

#### الاستعادة الكاملة
```bash
#!/bin/bash
# restore_full.sh

BACKUP_FILE="$1"
DB_NAME="handmade_ecommerce"

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup_file>"
    exit 1
fi

# إنشاء قاعدة بيانات جديدة
createdb -h localhost -U postgres $DB_NAME

# استعادة البيانات
pg_restore -h localhost -U postgres \
    --dbname=$DB_NAME \
    --verbose \
    --clean \
    --if-exists \
    --no-owner \
    --no-privileges \
    "$BACKUP_FILE"

echo "Database restored from $BACKUP_FILE"
```

#### الاستعادة النقطية (Point-in-Time Recovery)
```bash
#!/bin/bash
# point_in_time_restore.sh

TARGET_TIME="$1"  # Format: 2024-08-26 14:30:00
BASE_BACKUP="$2"
WAL_DIR="/backups/postgresql/wal"

if [ -z "$TARGET_TIME" ] || [ -z "$BASE_BACKUP" ]; then
    echo "Usage: $0 <target_time> <base_backup_path>"
    exit 1
fi

# إعداد ملف recovery
cat > /tmp/recovery.conf << EOF
restore_command = 'cp $WAL_DIR/%f %p'
recovery_target_time = '$TARGET_TIME'
recovery_target_action = 'promote'
EOF

# بدء عملية الاستعادة
pg_ctl start -D "$BASE_BACKUP" -o "-c config_file=/etc/postgresql/postgresql.conf"
```

---

## مراقبة الأداء

### 1. مراقبة الاستعلامات البطيئة

#### تمكين تسجيل الاستعلامات البطيئة
```sql
-- في postgresql.conf
-- log_min_duration_statement = 1000  -- أكثر من ثانية واحدة
-- log_statement = 'mod'
-- log_duration = on
-- log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '

-- إنشاء جدول لتتبع أداء الاستعلامات
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- استعلام للاستعلامات الأبطأ
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows,
    100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 20;
```

### 2. مراقبة استخدام الفهارس

#### تحليل استخدام الفهارس
```sql
-- فهارس غير مستخدمة
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE idx_scan = 0
ORDER BY schemaname, tablename;

-- فهارس تحتاج إعادة بناء
SELECT 
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats
WHERE schemaname NOT IN ('information_schema', 'pg_catalog')
ORDER BY abs(correlation) DESC;
```

### 3. مراقبة حجم الجداول

#### تحليل استخدام المساحة
```sql
-- أكبر الجداول
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) as index_size
FROM pg_tables
WHERE schemaname NOT IN ('information_schema', 'pg_catalog')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- إحصائيات الجداول
SELECT 
    schemaname,
    tablename,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes,
    n_live_tup as live_tuples,
    n_dead_tup as dead_tuples,
    last_vacuum,
    last_autovacuum,
    last_analyze,
    last_autoanalyze
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC;
```

### 4. تحسين الأداء التلقائي

#### إعدادات PostgreSQL المحسنة
```sql
-- في postgresql.conf
-- shared_buffers = 256MB                # 25% من الذاكرة
-- effective_cache_size = 1GB            # 75% من الذاكرة
-- work_mem = 4MB                        # للعمليات المعقدة
-- maintenance_work_mem = 64MB           # للصيانة
-- checkpoint_completion_target = 0.9
-- wal_buffers = 16MB
-- default_statistics_target = 100
-- random_page_cost = 1.1               # للـ SSD
-- effective_io_concurrency = 200       # للـ SSD
```

#### دوال الصيانة التلقائية
```sql
-- دالة لإعادة تنظيم الجداول
CREATE OR REPLACE FUNCTION auto_maintenance()
RETURNS void AS $$
DECLARE
    table_record RECORD;
BEGIN
    -- إعادة بناء الإحصائيات للجداول الكبيرة
    FOR table_record IN
        SELECT schemaname, tablename
        FROM pg_stat_user_tables
        WHERE n_dead_tup > 1000
    LOOP
        EXECUTE format('ANALYZE %I.%I', table_record.schemaname, table_record.tablename);
    END LOOP;
    
    -- تنظيف الجداول التي تحتاج صيانة
    FOR table_record IN
        SELECT schemaname, tablename
        FROM pg_stat_user_tables
        WHERE n_dead_tup > n_live_tup * 0.1  -- 10% dead tuples
    LOOP
        EXECUTE format('VACUUM ANALYZE %I.%I', table_record.schemaname, table_record.tablename);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- جدولة المهمة (باستخدام pg_cron إذا متوفر)
-- SELECT cron.schedule('auto-maintenance', '0 2 * * *', 'SELECT auto_maintenance();');
```

---

## خطة التطوير المستقبلي

### 1. التحسينات المخططة

#### Partitioning للجداول الكبيرة
```sql
-- تقسيم جدول الطلبات حسب التاريخ
CREATE TABLE order_processing.orders_2024 PARTITION OF order_processing.orders
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE order_processing.orders_2025 PARTITION OF order_processing.orders
FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
```

#### إضافة دعم للترجمة
```sql
-- جدول الترجمات
CREATE TABLE system_config.translations (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,  -- 'product', 'category', etc.
    entity_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,   -- 'name', 'description', etc.
    language_code VARCHAR(5) NOT NULL, -- 'ar', 'en', 'he'
    translation TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(entity_type, entity_id, field_name, language_code)
);
```

### 2. تحليلات متقدمة

#### جداول التحليلات والإحصائيات
```sql
-- جدول تتبع الأحداث
CREATE TABLE audit_logs.user_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_management.app_users(id),
    session_id VARCHAR(255),
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- فهرس للتحليلات
CREATE INDEX idx_user_events_type_time ON audit_logs.user_events(event_type, created_at);
CREATE INDEX idx_user_events_user_time ON audit_logs.user_events(user_id, created_at);
```

---

*تم إعداد هذا التقرير كجزء من مشروع التجارة الإلكترونية للمنتجات المصنوعة يدوياً*  
*إعداد: محمد الحجين | التاريخ: أغسطس 2024*  
*إصدار قاعدة البيانات: PostgreSQL 15.x*
