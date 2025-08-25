# How to Create a Page for Company 2101

## Method 1: Using REST API

Once the application is running, you can create a page for company 2101 by calling:

```bash
curl -X POST http://localhost:8080/api/test/company-page/create-company-2101
```

This will create a sample page with:
- **Company ID**: 2101
- **Slug**: "home" (default home page)
- **Title**: "الصفحة الرئيسية - متجر الحرف اليدوية"
- **Theme**: Blue primary color (#2563eb), Inter font
- **SEO**: Arabic meta tags optimized for handicraft store
- **Navigation**: Home, Products, About, Contact links
- **Social Links**: Facebook, Instagram, WhatsApp
- **Sections**:
  1. **Hero Section**: Welcome message with call-to-action
  2. **Product Grid**: 3-column layout showing products 2101-2106
  3. **Promo Section**: 25% discount offer

## Method 2: Using Command Line Runner

You can also run the application with a special argument:

```bash
java -jar demo.jar create-company-page
```

This will automatically create both:
- Home page (`/home`)
- About page (`/about`)

## Method 3: Direct Service Call

If you have access to the Spring context, you can call:

```java
@Autowired
private CompanyPageService companyPageService;

// Create the page data
PageUpdateDTO pageData = new PageUpdateDTO(
    "home",
    "الصفحة الرئيسية - متجر الحرف اليدوية",
    theme, seo, navigation, social, sections
);

// Create the page
PageViewDTO result = companyPageService.upsertDraft(2101L, pageData);
```

## Viewing the Created Page

After creation, you can view the page using:

```bash
# Get home page for company 2101
curl http://localhost:8080/api/test/company-page/2101/home

# Get about page for company 2101  
curl http://localhost:8080/api/test/company-page/2101/about
```

## Page Structure Created

The page will have the following sections:

### 1. Hero Section
- **Type**: HERO
- **Title**: "مرحباً بكم في متجر الحرف اليدوية الفلسطينية"
- **Subtitle**: "اكتشف عالم الإبداع والتراث الفلسطيني..."
- **CTA Button**: "اكتشف منتجاتنا" → `/products`
- **Background Image**: Sample media ID

### 2. Product Grid Section
- **Type**: GRID_PRODUCTS
- **Title**: "منتجاتنا المميزة"
- **Layout**: 3 columns
- **Products**: IDs 2101, 2102, 2103, 2104, 2105, 2106
- **Note**: These product IDs should exist in your database

### 3. Promotion Section
- **Type**: PROMO
- **Title**: "عرض خاص - خصم 25% على جميع المنتجات"
- **Badge**: "-25%"
- **Link**: `/products?sale=true`

## Database Tables Affected

The creation will insert/update data in these tables:
- `company_pages` - Main page record
- `company_page_nav` - Navigation links
- `company_page_social` - Social media links  
- `page_sections` - Page sections with JSONB content

## Notes

1. **Media IDs**: The sample uses placeholder UUIDs. Replace with actual media IDs from your `media_items` table.

2. **Product IDs**: The grid section references products 2101-2106. Ensure these products exist and belong to company 2101.

3. **Company Validation**: The system will validate that company 2101 exists in the `companies` table.

4. **Page Status**: Pages are created as DRAFT initially. You can publish them later using the publish endpoint.

5. **Theme Customization**: Modify the theme colors, fonts, and URLs as needed for the specific company brand.
