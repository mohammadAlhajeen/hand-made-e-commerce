# Company Page System - نظام صفحات الشركة

## Overview - نظرة عامة

This is a comprehensive company page management system built with Spring Boot and PostgreSQL. It provides:

- **Strong-typed JSONB content** using sealed interfaces
- **Ownership validation** for media and products
- **Flexible page sections** (Hero, Product Grid, Promo)
- **Theme and SEO management**
- **Navigation and social links**
- **Draft/Published workflow**

## Architecture - الهيكل المعماري

### Domain Entities - الكيانات الأساسية

```
📦 domain/page/
├── CompanyPage.java          # Main page entity
├── PageSection.java          # Individual sections with JSONB content
├── Theme.java               # Embedded theme configuration
├── Seo.java                 # Embedded SEO settings
├── NavigationLink.java      # Navigation menu items
├── SocialLink.java          # Social media links
├── PageStatus.java          # DRAFT | PUBLISHED
└── SectionType.java         # HERO | GRID_PRODUCTS | PROMO
```

### Content Types - أنواع المحتوى

```
📦 domain/page/content/
├── SectionContent.java      # Sealed interface for type safety
├── HeroContent.java         # Hero section with image and CTA
├── GridProductsContent.java # Product grid with ownership validation
└── PromoContent.java        # Promotional content
```

### DTOs and Views - كائنات النقل والعرض

```
📦 dto/page/
├── PageUpdateDTO.java       # Input for creating/updating pages
├── PageViewDTO.java         # Complete page response
├── SectionDTO.java          # Generic section wrapper
├── ThemeDTO.java            # Theme configuration
├── SeoDTO.java             # SEO metadata
├── NavigationLinkDTO.java   # Navigation items
└── SocialLinkDTO.java       # Social media links

📦 dto/page/view/
├── HeroViewContent.java     # Hero section with resolved image URLs
├── GridProductsViewContent.java # Product grid with resolved data
├── PromoViewContent.java    # Promotional content for display
└── ProductCardDTO.java      # Optimized product card data
```

## API Endpoints - نقاط النهاية

### Page Management - إدارة الصفحات

```http
# Get a page
GET /api/company/page/{slug}
Authorization: Bearer {jwt_token}

# Create/Update page (saves as draft)
PUT /api/company/page
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "slug": "home",
  "title": "الصفحة الرئيسية",
  "theme": {
    "primaryColor": "#2563eb",
    "secondaryColor": "#e5e7eb",
    "fontFamily": "Inter",
    "logoUrl": "https://cdn.example.com/logo.png",
    "coverUrl": "https://cdn.example.com/cover.png"
  },
  "seo": {
    "metaTitle": "شركة الحرف اليدوية",
    "metaDescription": "اكتشف أفضل المنتجات الحرفية",
    "ogImageUrl": "https://cdn.example.com/og-image.png"
  },
  "navigation": [
    {"label": "الرئيسية", "href": "/", "orderIndex": 1},
    {"label": "المنتجات", "href": "/products", "orderIndex": 2}
  ],
  "social": [
    {"platform": "facebook", "url": "https://facebook.com/company", "show": true, "orderIndex": 1}
  ],
  "sections": [
    {
      "type": "HERO",
      "orderIndex": 1,
      "content": {
        "kind": "HERO",
        "title": "مرحباً بكم في متجرنا",
        "subtitle": "اكتشف منتجاتنا المميزة",
        "imageMediaId": "550e8400-e29b-41d4-a716-446655440001",
        "ctaText": "تسوق الآن",
        "ctaHref": "/products"
      }
    },
    {
      "type": "GRID_PRODUCTS",
      "orderIndex": 2,
      "content": {
        "kind": "GRID_PRODUCTS",
        "title": "منتجاتنا المميزة",
        "columns": 3,
        "productIds": [101, 102, 103, 104, 105, 106]
      }
    },
    {
      "type": "PROMO",
      "orderIndex": 3,
      "content": {
        "kind": "PROMO",
        "title": "عرض خاص - خصم 25%",
        "badge": "-25%",
        "href": "/sale"
      }
    }
  ]
}

# Publish a page
POST /api/company/page/{slug}/publish
Authorization: Bearer {jwt_token}
```

## Key Features - الميزات الأساسية

### 1. Strong-Typed Content - محتوى قوي النوع

```java
// Input validation at compile-time
public sealed interface SectionContent 
    permits HeroContent, GridProductsContent, PromoContent {}

// Runtime validation
@AssertTrue(message="content.kind must match section type")
public boolean isTypeConsistent() {
    return content != null && type != null && switch (type) {
        case HERO -> content instanceof HeroContent;
        case GRID_PRODUCTS -> content instanceof GridProductsContent;
        case PROMO -> content instanceof PromoContent;
    };
}
```

### 2. Ownership Validation - التحقق من الملكية

```java
// Media ownership check
var media = mediaRepo.findActiveById(heroContent.imageMediaId())
    .orElseThrow(() -> new EntityNotFoundException("Image not found"));
if (!media.getUserId().equals(companyId))
    throw new IllegalStateException("You don't own this image");

// Product ownership check
var products = productRepo.findAllById(gridContent.productIds());
boolean sameCompany = products.stream()
    .allMatch(p -> p.getCompany().getId().equals(companyId));
if (!sameCompany)
    throw new IllegalStateException("Products must belong to your company");
```

### 3. Efficient Data Loading - تحميل البيانات بكفاءة

```java
// Projection interface for optimized queries
public interface ProductCardView {
    Long getId();
    String getName();
    BigDecimal getPrice();
    String getMainImageUrl();
}

// Order-preserving query
@Query("""
    select p.id as id, p.name as name, p.price as price,
           (SELECT media.absoluteUrl FROM ProductImage img
            LEFT JOIN img.media media
            WHERE img.product = p AND img.main = true) as mainImageUrl
    from Product p
    where p.company.id = :companyId and p.id in :ids
    order by CASE 
        WHEN p.id = :ids[0] THEN 0
        WHEN p.id = :ids[1] THEN 1
        ...
    END
""")
List<ProductCardView> findCardsForCompanyByIdsOrdered(Long companyId, List<Long> ids);
```

### 4. JSONB Storage - تخزين JSONB

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb", nullable = false)
private SectionContent content;
```

```sql
-- GIN index for efficient JSONB queries
CREATE INDEX ix_sections_content_gin 
ON page_sections USING GIN (content jsonb_path_ops);
```

## Database Schema - مخطط قاعدة البيانات

```sql
-- Main page table
CREATE TABLE company_pages (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    slug VARCHAR(64) NOT NULL,
    title VARCHAR(120) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    version BIGINT,  -- Optimistic locking
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,
    
    -- Embedded theme
    theme_primary_color VARCHAR(16),
    theme_secondary_color VARCHAR(16),
    theme_font_family VARCHAR(64),
    theme_logo_url TEXT,
    theme_cover_url TEXT,
    
    -- Embedded SEO
    seo_meta_title VARCHAR(70),
    seo_meta_description VARCHAR(160),
    seo_og_image_url TEXT,
    
    CONSTRAINT uk_company_slug UNIQUE (company_id, slug)
);

-- Navigation links (collection table)
CREATE TABLE company_page_nav (
    page_id BIGINT NOT NULL REFERENCES company_pages(id) ON DELETE CASCADE,
    label TEXT NOT NULL,
    href TEXT NOT NULL,
    order_index INT,
    PRIMARY KEY (page_id, label)
);

-- Social links (collection table)
CREATE TABLE company_page_social (
    page_id BIGINT NOT NULL REFERENCES company_pages(id) ON DELETE CASCADE,
    platform TEXT,
    url TEXT,
    "show" BOOLEAN,
    order_index INT
);

-- Page sections with JSONB content
CREATE TABLE page_sections (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL REFERENCES company_pages(id) ON DELETE CASCADE,
    type VARCHAR(24) NOT NULL,
    order_index INT NOT NULL,
    content JSONB NOT NULL
);
```

## Security and Validation - الأمان والتحقق

1. **Authentication**: JWT-based authentication
2. **Authorization**: Company-scoped data access
3. **Content Validation**: Bean validation on records
4. **Ownership Checks**: Media and product ownership validation
5. **Type Safety**: Sealed interfaces prevent invalid content types

## Performance Optimizations - تحسينات الأداء

1. **Projections**: Fetch only required fields for product cards
2. **JSONB Indexing**: GIN indexes for content queries
3. **Lazy Loading**: Fetch associations only when needed
4. **Order Preservation**: Efficient product ordering in grids
5. **Connection Pooling**: Optimized database connections

## Usage Examples - أمثلة الاستخدام

See `CompanyPageExamples.java` for comprehensive examples of:
- Creating page content
- Handling different section types
- Expected API responses
- Content transformation flow

## Future Enhancements - التحسينات المستقبلية

1. **Additional Section Types**: Video, Gallery, Testimonials
2. **Page Templates**: Pre-built page layouts
3. **A/B Testing**: Multiple page versions
4. **Analytics Integration**: Page performance tracking
5. **Content Scheduling**: Publish pages at specific times

---

Built with ❤️ using Spring Boot 3, PostgreSQL, and modern Java features.
