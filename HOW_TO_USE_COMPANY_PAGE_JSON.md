# استخدام JSON لإنشاء صفحة الشركة

## باستخدام curl

```bash
curl -X POST http://localhost:8080/company/page \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d @company-page-create.json
```

## باستخدام Postman

1. **Method**: POST
2. **URL**: `http://localhost:8080/company/page`
3. **Headers**: 
   - `Content-Type: application/json`
   - `Authorization: Bearer YOUR_JWT_TOKEN`
4. **Body**: Raw JSON - نسخ محتوى ملف `company-page-create.json`

## محتويات الـ JSON

### معرف الصورة المطلوب
- **Media UUID**: `10c4c401-c498-407d-a24a-39017f25e3c6`

### معرفات المنتجات المطلوبة (80-95)
- **Grid 1**: جميع المنتجات من 80 إلى 95 (16 منتج)
- **Grid 2**: مختارة من 80, 82, 84, 86, 88, 90, 92, 94 (8 منتجات)

### أقسام الصفحة المنشأة

1. **Hero Section 1**: ترحيب بالزوار مع الصورة المحددة
2. **Product Grid 1**: عرض 16 منتج (3 أعمدة)
3. **Promo Section**: عرض خصم 30%
4. **Hero Section 2**: قصة التراث الفلسطيني
5. **Product Grid 2**: الأكثر مبيعاً (4 أعمدة)

### ملاحظات مهمة

1. **JWT Token**: تحتاج لرمز المصادقة للشركة
2. **Media Validation**: سيتم التحقق من وجود الصورة `10c4c401-c498-407d-a24a-39017f25e3c6`
3. **Product Validation**: سيتم التحقق من وجود المنتجات 80-95 وأنها تنتمي للشركة
4. **Response**: ستحصل على `PageViewDTO` مع تفاصيل الصفحة المنشأة

### مثال على الاستجابة المتوقعة

```json
{
  "id": 1,
  "slug": "home",
  "title": "الصفحة الرئيسية - متجر الحرف اليدوية المميز",
  "status": "DRAFT",
  "version": 1,
  "updatedAt": "2025-08-23T10:30:00Z",
  "publishedAt": null,
  "sections": [
    {
      "type": "HERO",
      "orderIndex": 1,
      "content": {
        "kind": "HERO",
        "title": "مرحباً بكم في متجر الحرف اليدوية الفلسطينية",
        "imageUrl": "https://resolved-media-url.com/image.jpg"
      }
    }
    // ... باقي الأقسام
  ]
}
```
