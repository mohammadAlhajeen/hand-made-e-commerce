# Chapter 6: API Documentation and Service Interfaces

## 6.1 API Documentation Overview

This chapter provides comprehensive documentation for all REST API endpoints in the Hand-Made E-Commerce System. The APIs follow RESTful principles and provide standardized interfaces for client applications to interact with the system.

### 6.1.1 API Design Principles

1. **RESTful Architecture**: Standard HTTP methods (GET, POST, PUT, DELETE) with meaningful URLs
2. **Consistent Response Format**: Standardized JSON response structure across all endpoints
3. **Error Handling**: Comprehensive error responses with appropriate HTTP status codes
4. **Versioning**: API versioning support for backward compatibility
5. **Documentation**: OpenAPI/Swagger specification for interactive documentation

### 6.1.2 Base Configuration
| 500 | Internal Server Error | Server error |

---

## Authentication APIs

### 1. User Login

**Endpoint**: `POST /auth/login`

**Description**: Authenticate user and get access token

**Authorization**: Public

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "أحمد",
    "lastName": "محمد",
    "role": "CUSTOMER",
    "isActive": true
  }
}
```

**Error Response** (401 Unauthorized):
```json
{
  "error": "INVALID_CREDENTIALS",
  "message": "Invalid email or password",
  "timestamp": "2024-01-20T10:30:00Z"
}
```

---

## Authentication APIs

### POST /authcontroller/register
تسجيل شركة جديدة في النظام

**Headers:**
```http
Content-Type: application/json
```

**Request Body:**
```json
{
    "username": "handmade_store",
    "email": "store@example.com",
    "password": "SecurePass123!",
    "firstName": "محمد",
    "lastName": "الحرفي",
    "phone": "+970599123456",
    "companyName": "متجر الحرف اليدوية",
    "description": "متخصصون في صناعة المنتجات التراثية والحرف اليدوية الأصيلة"
}
```

**Validation Rules:**
- `username`: 3-50 حرف، فريد، حروف وأرقام فقط
- `email`: صيغة إيميل صحيحة، فريد
- `password`: 8 أحرف على الأقل، يحتوي على حروف كبيرة وصغيرة ورقم
- `phone`: صيغة رقم هاتف صحيحة
- `companyName`: 2-100 حرف

**Response (201 Created):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYW5kbWFkZV9zdG9yZSIsImlhdCI6MTY5MzAwOTIwMCwiZXhwIjoxNjkzMDk1NjAwfQ.example",
    "type": "Bearer",
    "username": "handmade_store",
    "roles": ["COMPANY"],
    "expiresIn": 86400
}
```

**Error Responses:**
```json
// 400 - Username already exists
{
    "error": "Username already exists",
    "message": "اسم المستخدم موجود بالفعل",
    "timestamp": "2024-08-26T10:30:00Z"
}

// 400 - Validation error
{
    "error": "Validation failed",
    "message": "البيانات المدخلة غير صحيحة",
    "details": {
        "password": "كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل",
        "email": "صيغة الإيميل غير صحيحة"
    },
    "timestamp": "2024-08-26T10:30:00Z"
}
```

### POST /authcontroller/login
تسجيل الدخول للنظام

**Request Body:**
```json
{
    "username": "handmade_store",
    "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "handmade_store",
    "roles": ["COMPANY"],
    "expiresIn": 86400,
    "user": {
        "id": 1,
        "username": "handmade_store",
        "email": "store@example.com",
        "firstName": "محمد",
        "lastName": "الحرفي",
        "companyName": "متجر الحرف اليدوية"
    }
}
```

**Error Response:**
```json
// 401 - Invalid credentials
{
    "error": "Invalid credentials",
    "message": "اسم المستخدم أو كلمة المرور غير صحيحة",
    "timestamp": "2024-08-26T10:30:00Z"
}
```

---

## Product Management APIs

### POST /company/create-instock-product
إنشاء منتج جاهز للبيع

**Authentication:** Required (COMPANY role)

**Request Body:**
```json
{
    "name": "سجادة يدوية تراثية",
    "description": "سجادة مصنوعة يدوياً بتصاميم تراثية أصيلة، مصنوعة من أجود أنواع الصوف الطبيعي",
    "basePrice": 250.00,
    "quantityInStock": 15,
    "minStockLevel": 3,
    "categoryId": 5,
    "tags": ["تراثي", "يدوي", "صوف", "سجاد"],
    "images": [
        {
            "imageId": "550e8400-e29b-41d4-a716-446655440001",
            "isMain": true
        },
        {
            "imageId": "550e8400-e29b-41d4-a716-446655440002", 
            "isMain": false
        }
    ],
    "attributes": [
        {
            "name": "الحجم",
            "type": "SELECT",
            "required": true,
            "orderIndex": 1,
            "values": [
                {
                    "value": "صغير (120x80 سم)",
                    "extraPrice": 0.00
                },
                {
                    "value": "متوسط (200x150 سم)",
                    "extraPrice": 50.00
                },
                {
                    "value": "كبير (300x200 سم)",
                    "extraPrice": 120.00
                }
            ]
        },
        {
            "name": "اللون الأساسي",
            "type": "SELECT",
            "required": true,
            "orderIndex": 2,
            "values": [
                {
                    "value": "أحمر تراثي",
                    "extraPrice": 0.00
                },
                {
                    "value": "أزرق داكن",
                    "extraPrice": 10.00
                },
                {
                    "value": "بني طبيعي",
                    "extraPrice": 15.00
                }
            ]
        },
        {
            "name": "خدمات إضافية",
            "type": "MULTI_SELECT",
            "required": false,
            "orderIndex": 3,
            "values": [
                {
                    "value": "تغليف فاخر",
                    "extraPrice": 25.00
                },
                {
                    "value": "شهادة أصالة",
                    "extraPrice": 15.00
                },
                {
                    "value": "توصيل سريع",
                    "extraPrice": 30.00
                }
            ]
        }
    ]
}
```

**Response (201 Created):**
```json
{
    "id": 123,
    "name": "سجادة يدوية تراثية",
    "description": "سجادة مصنوعة يدوياً...",
    "basePrice": 250.00,
    "productType": "IN_STOCK",
    "quantityInStock": 15,
    "companyId": 1,
    "companyName": "متجر الحرف اليدوية",
    "category": {
        "id": 5,
        "name": "منسوجات",
        "slug": "textiles"
    },
    "tags": ["تراثي", "يدوي", "صوف", "سجاد"],
    "images": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440001",
            "url": "https://example.com/media/carpet1.jpg",
            "isMain": true
        }
    ],
    "attributes": [
        {
            "id": 45,
            "name": "الحجم",
            "type": "SELECT",
            "required": true,
            "values": [
                {
                    "id": 101,
                    "value": "صغير (120x80 سم)",
                    "extraPrice": 0.00
                }
            ]
        }
    ],
    "createdAt": "2024-08-26T10:30:00Z",
    "updatedAt": "2024-08-26T10:30:00Z"
}
```

### POST /company/create-preorder-product
إنشاء منتج طلب مسبق

**Request Body:**
```json
{
    "name": "خاتم ذهبي مخصص",
    "description": "خاتم ذهبي مصنوع حسب الطلب بتصميم شخصي",
    "basePrice": 800.00,
    "estimatedDays": 21,
    "depositPercentage": 30.00,
    "maxPreOrders": 5,
    "categoryId": 3,
    "tags": ["ذهب", "مخصص", "خاتم"],
    "images": [
        {
            "imageId": "550e8400-e29b-41d4-a716-446655440003",
            "isMain": true
        }
    ],
    "attributes": [
        {
            "name": "عيار الذهب",
            "type": "SELECT",
            "required": true,
            "values": [
                {
                    "value": "18 قيراط",
                    "extraPrice": 0.00
                },
                {
                    "value": "21 قيراط",
                    "extraPrice": 200.00
                }
            ]
        },
        {
            "name": "النقش",
            "type": "TEXT",
            "required": false,
            "values": []
        }
    ]
}
```

**Response (201 Created):**
```json
{
    "id": 124,
    "name": "خاتم ذهبي مخصص",
    "basePrice": 800.00,
    "productType": "PRE_ORDER",
    "estimatedDays": 21,
    "depositPercentage": 30.00,
    "depositAmount": 240.00,
    "maxPreOrders": 5,
    "currentPreOrders": 0,
    "companyId": 1,
    "createdAt": "2024-08-26T10:30:00Z"
}
```

### GET /company/products
عرض منتجات الشركة

**Query Parameters:**
- `page` (int): رقم الصفحة (افتراضي: 0)
- `size` (int): عدد العناصر (افتراضي: 20)
- `type` (string): نوع المنتج (`IN_STOCK`, `PRE_ORDER`)
- `category` (string): اسم التصنيف
- `search` (string): البحث في الاسم والوصف

**Response (200 OK):**
```json
{
    "content": [
        {
            "id": 123,
            "name": "سجادة يدوية تراثية",
            "basePrice": 250.00,
            "productType": "IN_STOCK",
            "quantityInStock": 15,
            "mainImageUrl": "https://example.com/media/carpet1.jpg",
            "category": "منسوجات",
            "avgRating": 4.5,
            "reviewCount": 12,
            "createdAt": "2024-08-26T10:30:00Z"
        }
    ],
    "pageable": {
        "sort": {
            "sorted": true,
            "direction": "DESC",
            "property": "createdAt"
        },
        "offset": 0,
        "pageSize": 20,
        "pageNumber": 0
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "size": 20,
    "number": 0,
    "first": true,
    "numberOfElements": 1
}
```

### PUT /company/products/{productId}
تحديث منتج

**Request Body:**
```json
{
    "name": "سجادة يدوية تراثية محدثة",
    "description": "وصف محدث للمنتج",
    "basePrice": 275.00,
    "quantityInStock": 20
}
```

**Response (200 OK):**
```json
{
    "id": 123,
    "name": "سجادة يدوية تراثية محدثة",
    "basePrice": 275.00,
    "updatedAt": "2024-08-26T11:00:00Z"
}
```

### DELETE /company/products/{productId}
حذف منتج

**Response (204 No Content)**

---

## Cart Management APIs

### GET /api/cart/{companyId}
عرض سلة شركة معينة

**Authentication:** Required (CUSTOMER role)

**Response (200 OK):**
```json
{
    "cartId": 15,
    "companyId": 1,
    "companyName": "متجر الحرف اليدوية",
    "items": [
        {
            "itemId": 45,
            "productId": 123,
            "productName": "سجادة يدوية تراثية",
            "productType": "IN_STOCK",
            "quantity": 2,
            "unitPrice": 250.00,
            "extraPrice": 50.00,
            "totalPrice": 600.00,
            "depositRequired": false,
            "depositAmount": 0.00,
            "remainingAmount": 600.00,
            "mainImageUrl": "https://example.com/media/carpet1.jpg",
            "selections": [
                {
                    "attributeId": 45,
                    "attributeName": "الحجم",
                    "selectedValues": [
                        {
                            "valueId": 102,
                            "value": "متوسط (200x150 سم)",
                            "extraPrice": 50.00
                        }
                    ],
                    "extraPrice": 50.00
                }
            ]
        },
        {
            "itemId": 46,
            "productId": 124,
            "productName": "خاتم ذهبي مخصص",
            "productType": "PRE_ORDER",
            "quantity": 1,
            "unitPrice": 800.00,
            "extraPrice": 200.00,
            "totalPrice": 1000.00,
            "depositRequired": true,
            "depositAmount": 300.00,
            "remainingAmount": 700.00,
            "selections": [
                {
                    "attributeId": 46,
                    "attributeName": "عيار الذهب",
                    "selectedValues": [
                        {
                            "valueId": 105,
                            "value": "21 قيراط",
                            "extraPrice": 200.00
                        }
                    ]
                }
            ]
        }
    ],
    "summary": {
        "totalItems": 3,
        "subtotal": 1600.00,
        "totalExtraPrice": 250.00,
        "grandTotal": 1600.00,
        "totalDeposit": 300.00,
        "totalRemaining": 1300.00
    },
    "createdAt": "2024-08-26T09:00:00Z",
    "updatedAt": "2024-08-26T10:30:00Z"
}
```

### POST /api/cart/{companyId}/add
إضافة منتج للسلة

**Request Body:**
```json
{
    "productId": 123,
    "quantity": 2,
    "selections": [
        {
            "attributeId": 45,
            "selectedValueIds": [102]
        },
        {
            "attributeId": 47,
            "selectedValueIds": [108, 109]
        }
    ]
}
```

**Validation Rules:**
- `productId`: يجب أن يكون موجود وينتمي للشركة المحددة
- `quantity`: رقم موجب، لا يتجاوز المخزون المتاح
- `selections`: يجب أن تحتوي على جميع السمات المطلوبة

**Response (200 OK):**
```json
{
    "itemId": 45,
    "message": "تم إضافة المنتج للسلة بنجاح",
    "cartSummary": {
        "totalItems": 3,
        "grandTotal": 1600.00
    }
}
```

**Error Responses:**
```json
// 400 - Out of stock
{
    "error": "Insufficient stock",
    "message": "الكمية المطلوبة غير متوفرة في المخزون",
    "availableQuantity": 8,
    "requestedQuantity": 10
}

// 400 - Missing required attribute
{
    "error": "Required attribute missing",
    "message": "يجب اختيار قيمة للسمة المطلوبة",
    "missingAttribute": "الحجم"
}
```

### PUT /api/cart/items/{itemId}
تحديث عنصر في السلة

**Request Body:**
```json
{
    "quantity": 3,
    "selections": [
        {
            "attributeId": 45,
            "selectedValueIds": [103]
        }
    ]
}
```

**Response (200 OK):**
```json
{
    "itemId": 45,
    "quantity": 3,
    "unitPrice": 250.00,
    "extraPrice": 120.00,
    "totalPrice": 1110.00,
    "message": "تم تحديث العنصر بنجاح"
}
```

### DELETE /api/cart/items/{itemId}
حذف عنصر من السلة

**Response (204 No Content)**

### DELETE /api/cart/{companyId}/clear
إفراغ السلة

**Response (200 OK):**
```json
{
    "message": "تم إفراغ السلة بنجاح",
    "cartId": 15
}
```

---

## Order Management APIs

### POST /api/orders/checkout/{cartId}
إتمام الطلب من السلة

**Authentication:** Required (CUSTOMER role)

**Request Body (Optional):**
```json
{
    "shippingAddress": {
        "street": "شارع الاستقلال",
        "city": "رام الله",
        "region": "الضفة الغربية",
        "postalCode": "12345",
        "country": "فلسطين",
        "phone": "+970599123456"
    },
    "notes": "يرجى التعامل بحذر مع المنتجات الهشة"
}
```

**Response (201 Created):**
```json
{
    "orderId": 567,
    "orderNumber": "ORD-2024-000567",
    "status": "PENDING_DEPOSIT",
    "customerId": 25,
    "companyId": 1,
    "companyName": "متجر الحرف اليدوية",
    "items": [
        {
            "orderItemId": 890,
            "productId": 123,
            "productName": "سجادة يدوية تراثية",
            "productType": "IN_STOCK",
            "quantity": 2,
            "unitPrice": 250.00,
            "extraPrice": 50.00,
            "totalPrice": 600.00,
            "depositRequired": false,
            "depositAmount": 0.00,
            "allocatedQuantity": 2,
            "shippedQuantity": 0,
            "selections": [...]
        },
        {
            "orderItemId": 891,
            "productId": 124,
            "productName": "خاتم ذهبي مخصص",
            "productType": "PRE_ORDER",
            "quantity": 1,
            "unitPrice": 800.00,
            "extraPrice": 200.00,
            "totalPrice": 1000.00,
            "depositRequired": true,
            "depositAmount": 300.00,
            "allocatedQuantity": 0,
            "shippedQuantity": 0
        }
    ],
    "summary": {
        "totalAmount": 1600.00,
        "depositAmount": 300.00,
        "depositPaid": 0.00,
        "remainingAmount": 1300.00
    },
    "shippingAddress": {
        "street": "شارع الاستقلال",
        "city": "رام الله",
        "region": "الضفة الغربية"
    },
    "estimatedDelivery": "2024-09-16T00:00:00Z",
    "createdAt": "2024-08-26T10:30:00Z"
}
```

### POST /api/orders/{orderId}/pay-deposit
دفع العربون

**Request Body:**
```json
{
    "paymentMethod": "FAKE_PAYMENT",
    "amount": 300.00,
    "paymentDetails": {
        "cardNumber": "4*** **** **** 1234",
        "transactionId": "TXN_12345678"
    }
}
```

**Response (200 OK):**
```json
{
    "orderId": 567,
    "paymentId": "PAY_87654321",
    "paidAmount": 300.00,
    "newStatus": "DEPOSIT_PAID",
    "remainingAmount": 1300.00,
    "message": "تم دفع العربون بنجاح",
    "paymentDate": "2024-08-26T10:35:00Z"
}
```

### GET /api/orders
عرض طلبات العميل

**Query Parameters:**
- `page` (int): رقم الصفحة
- `size` (int): عدد العناصر
- `status` (string): حالة الطلب
- `companyId` (long): معرف الشركة

**Response (200 OK):**
```json
{
    "content": [
        {
            "orderId": 567,
            "orderNumber": "ORD-2024-000567",
            "status": "DEPOSIT_PAID",
            "companyName": "متجر الحرف اليدوية",
            "totalAmount": 1600.00,
            "depositPaid": 300.00,
            "remainingAmount": 1300.00,
            "itemsCount": 2,
            "estimatedDelivery": "2024-09-16T00:00:00Z",
            "createdAt": "2024-08-26T10:30:00Z"
        }
    ],
    "totalElements": 1,
    "totalPages": 1
}
```

### GET /api/orders/{orderId}
عرض تفاصيل طلب محدد

**Response (200 OK):**
```json
{
    "orderId": 567,
    "orderNumber": "ORD-2024-000567",
    "status": "PROCESSING",
    "statusHistory": [
        {
            "status": "PENDING_DEPOSIT",
            "timestamp": "2024-08-26T10:30:00Z",
            "note": "تم إنشاء الطلب"
        },
        {
            "status": "DEPOSIT_PAID",
            "timestamp": "2024-08-26T10:35:00Z",
            "note": "تم دفع العربون"
        },
        {
            "status": "CONFIRMED",
            "timestamp": "2024-08-26T11:00:00Z",
            "note": "تم تأكيد الطلب من الشركة"
        },
        {
            "status": "PROCESSING",
            "timestamp": "2024-08-26T12:00:00Z",
            "note": "جاري تحضير الطلب"
        }
    ],
    "customer": {
        "name": "أحمد محمد",
        "email": "ahmed@example.com",
        "phone": "+970599123456"
    },
    "company": {
        "id": 1,
        "name": "متجر الحرف اليدوية",
        "email": "store@example.com"
    },
    "items": [...],
    "payments": [
        {
            "paymentId": "PAY_87654321",
            "type": "DEPOSIT",
            "amount": 300.00,
            "method": "FAKE_PAYMENT",
            "status": "COMPLETED",
            "paidAt": "2024-08-26T10:35:00Z"
        }
    ],
    "shipments": [],
    "timeline": [
        {
            "event": "ORDER_CREATED",
            "timestamp": "2024-08-26T10:30:00Z",
            "description": "تم إنشاء الطلب"
        },
        {
            "event": "DEPOSIT_PAID",
            "timestamp": "2024-08-26T10:35:00Z",
            "description": "تم دفع العربون 300.00 ش.ج"
        }
    ]
}
```

### Company Order Management

### GET /company/orders
عرض طلبات الشركة

**Authentication:** Required (COMPANY role)

**Query Parameters:**
- `status` (string): فلترة حسب الحالة
- `dateFrom` (date): من تاريخ
- `dateTo` (date): إلى تاريخ

**Response (200 OK):**
```json
{
    "content": [
        {
            "orderId": 567,
            "orderNumber": "ORD-2024-000567",
            "customerName": "أحمد محمد",
            "status": "DEPOSIT_PAID",
            "totalAmount": 1600.00,
            "depositPaid": 300.00,
            "itemsCount": 2,
            "requiresAction": true,
            "actionRequired": "CONFIRM_ORDER",
            "createdAt": "2024-08-26T10:30:00Z"
        }
    ]
}
```

### POST /company/orders/{orderId}/confirm
تأكيد الطلب من الشركة

**Request Body:**
```json
{
    "estimatedCompletionDays": 14,
    "notes": "سيتم البدء في الإنتاج فوراً"
}
```

### POST /company/orders/{orderId}/shipments
إنشاء شحنة

**Request Body:**
```json
{
    "items": [
        {
            "orderItemId": 890,
            "quantity": 2
        }
    ],
    "trackingNumber": "TRK123456789",
    "shippingCompany": "شركة التوصيل السريع",
    "notes": "تم تغليف المنتجات بعناية"
}
```

**Response (201 Created):**
```json
{
    "shipmentId": 123,
    "trackingNumber": "TRK123456789",
    "status": "SHIPPED",
    "items": [
        {
            "orderItemId": 890,
            "productName": "سجادة يدوية تراثية",
            "quantity": 2
        }
    ],
    "shippedAt": "2024-08-26T14:00:00Z",
    "estimatedDelivery": "2024-08-28T00:00:00Z"
}
```

---

## Company Page APIs

### GET /companyPages/{companyId}/{slug}
عرض صفحة الشركة (عامة)

**Response (200 OK):**
```json
{
    "id": 5,
    "companyId": 1,
    "title": "متجر الحرف اليدوية الأصيلة",
    "slug": "authentic-handicrafts",
    "description": "متخصصون في صناعة وبيع الحرف اليدوية التراثية",
    "status": "PUBLISHED",
    "seo": {
        "metaTitle": "متجر الحرف اليدوية - أفضل المنتجات التراثية",
        "metaDescription": "تسوق من مجموعة واسعة من المنتجات اليدوية الأصيلة المصنوعة بعناية فائقة",
        "keywords": ["حرف يدوية", "تراثي", "أصيل", "فلسطيني", "منتجات تراثية"]
    },
    "theme": {
        "primaryColor": "#8B4513",
        "secondaryColor": "#D2691E",
        "accentColor": "#CD853F",
        "fontFamily": "Amiri",
        "logoUrl": "https://example.com/media/logo.png"
    },
    "navigation": [
        {
            "label": "الرئيسية",
            "href": "/",
            "orderIndex": 1
        },
        {
            "label": "منتجاتنا",
            "href": "/products",
            "orderIndex": 2
        },
        {
            "label": "من نحن",
            "href": "/about",
            "orderIndex": 3
        },
        {
            "label": "اتصل بنا",
            "href": "/contact",
            "orderIndex": 4
        }
    ],
    "socialLinks": [
        {
            "platform": "FACEBOOK",
            "url": "https://facebook.com/handicrafts.store",
            "show": true,
            "orderIndex": 1
        },
        {
            "platform": "INSTAGRAM",
            "url": "https://instagram.com/handicrafts_store",
            "show": true,
            "orderIndex": 2
        }
    ],
    "sections": [
        {
            "id": 12,
            "type": "HERO",
            "orderIndex": 1,
            "content": {
                "title": "أهلاً وسهلاً بكم في عالم الحرف اليدوية الأصيلة",
                "subtitle": "اكتشفوا مجموعتنا الفريدة من المنتجات التراثية المصنوعة بأيدي ماهرة",
                "backgroundImageUrl": "https://example.com/media/hero-bg.jpg",
                "overlayOpacity": 0.4,
                "textColor": "#FFFFFF",
                "ctaText": "تسوق الآن",
                "ctaLink": "/products",
                "ctaStyle": "PRIMARY"
            }
        },
        {
            "id": 13,
            "type": "GRID_PRODUCTS",
            "orderIndex": 2,
            "content": {
                "title": "منتجاتنا المميزة",
                "description": "تصفحوا مجموعة مختارة من أفضل منتجاتنا",
                "displayType": "FEATURED",
                "gridColumns": 3,
                "showPrices": true,
                "showRatings": true,
                "maxProducts": 6,
                "products": [
                    {
                        "id": 123,
                        "name": "سجادة يدوية تراثية",
                        "basePrice": 250.00,
                        "imageUrl": "https://example.com/media/carpet1.jpg",
                        "avgRating": 4.5,
                        "reviewCount": 12
                    }
                ]
            }
        },
        {
            "id": 14,
            "type": "PROMO",
            "orderIndex": 3,
            "content": {
                "title": "خصم خاص للعملاء الجدد",
                "description": "احصلوا على خصم 15% على أول طلب لكم",
                "promoCode": "WELCOME15",
                "discountPercentage": 15,
                "validUntil": "2024-12-31T23:59:59Z",
                "backgroundImageUrl": "https://example.com/media/promo-bg.jpg",
                "ctaText": "استخدم الكود",
                "ctaLink": "/products"
            }
        }
    ],
    "contact": {
        "email": "info@handicrafts-store.com",
        "phone": "+970599123456",
        "whatsapp": "+970599123456",
        "address": "رام الله، الضفة الغربية، فلسطين"
    },
    "stats": {
        "productsCount": 45,
        "avgRating": 4.7,
        "totalReviews": 156,
        "yearsOfExperience": 8
    },
    "lastUpdated": "2024-08-26T12:00:00Z"
}
```

### Company Page Management (للشركات)

### GET /company/page
عرض صفحة الشركة للتحرير

**Authentication:** Required (COMPANY role)

### PUT /company/page
تحديث صفحة الشركة

**Request Body:**
```json
{
    "title": "متجر الحرف اليدوية المحدث",
    "slug": "updated-handicrafts",
    "seo": {
        "metaTitle": "عنوان محدث",
        "metaDescription": "وصف محدث",
        "keywords": ["كلمة1", "كلمة2"]
    },
    "theme": {
        "primaryColor": "#8B4513",
        "secondaryColor": "#D2691E"
    },
    "navigation": [...],
    "socialLinks": [...],
    "sections": [...]
}
```

### POST /company/page/sections
إضافة قسم جديد للصفحة

**Request Body:**
```json
{
    "type": "HERO",
    "orderIndex": 1,
    "content": {
        "title": "عنوان جديد",
        "subtitle": "نص فرعي",
        "backgroundImageUrl": "https://example.com/image.jpg"
    }
}
```

---

## Public APIs

### GET /api/products
تصفح المنتجات (عام)

**Query Parameters:**
- `page` (int, default: 0): رقم الصفحة
- `size` (int, default: 20): عدد العناصر
- `category` (string): اسم التصنيف أو slug
- `tag` (string): اسم العلامة
- `search` (string): البحث في الاسم والوصف
- `minPrice` (decimal): الحد الأدنى للسعر
- `maxPrice` (decimal): الحد الأقصى للسعر
- `company` (string): اسم الشركة
- `sort` (string): ترتيب النتائج (`price_asc`, `price_desc`, `rating_desc`, `newest`, `oldest`)
- `inStock` (boolean): المنتجات المتوفرة فقط

**Response (200 OK):**
```json
{
    "content": [
        {
            "id": 123,
            "name": "سجادة يدوية تراثية",
            "description": "سجادة مصنوعة يدوياً...",
            "basePrice": 250.00,
            "productType": "IN_STOCK",
            "companyId": 1,
            "companyName": "متجر الحرف اليدوية",
            "companySlug": "handicrafts-store",
            "category": {
                "id": 5,
                "name": "منسوجات",
                "slug": "textiles"
            },
            "tags": ["تراثي", "يدوي", "صوف"],
            "mainImageUrl": "https://example.com/media/carpet1.jpg",
            "avgRating": 4.5,
            "reviewCount": 12,
            "isInStock": true,
            "hasVariations": true,
            "priceRange": {
                "min": 250.00,
                "max": 370.00
            },
            "createdAt": "2024-08-26T10:30:00Z"
        }
    ],
    "pageable": {
        "sort": {
            "sorted": true,
            "direction": "DESC",
            "property": "avgRating"
        },
        "offset": 0,
        "pageSize": 20,
        "pageNumber": 0
    },
    "totalElements": 45,
    "totalPages": 3,
    "last": false,
    "size": 20,
    "number": 0,
    "first": true,
    "numberOfElements": 20,
    "filters": {
        "categories": [
            {
                "id": 5,
                "name": "منسوجات",
                "count": 12
            }
        ],
        "companies": [
            {
                "id": 1,
                "name": "متجر الحرف اليدوية",
                "count": 8
            }
        ],
        "priceRanges": [
            {
                "range": "0-100",
                "count": 15
            },
            {
                "range": "100-500",
                "count": 25
            }
        ]
    }
}
```

### GET /api/products/{productId}
عرض تفاصيل منتج

**Response (200 OK):**
```json
{
    "id": 123,
    "name": "سجادة يدوية تراثية",
    "description": "سجادة مصنوعة يدوياً بتصاميم تراثية أصيلة...",
    "basePrice": 250.00,
    "productType": "IN_STOCK",
    "quantityInStock": 15,
    "company": {
        "id": 1,
        "name": "متجر الحرف اليدوية",
        "slug": "handicrafts-store",
        "avgRating": 4.7,
        "reviewCount": 156,
        "profileImageUrl": "https://example.com/media/company-logo.jpg"
    },
    "category": {
        "id": 5,
        "name": "منسوجات",
        "slug": "textiles",
        "path": "الرئيسية > منتجات منزلية > منسوجات"
    },
    "tags": ["تراثي", "يدوي", "صوف", "سجاد"],
    "images": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440001",
            "url": "https://example.com/media/carpet1.jpg",
            "isMain": true,
            "alt": "سجادة يدوية تراثية - منظر أمامي"
        },
        {
            "id": "550e8400-e29b-41d4-a716-446655440002",
            "url": "https://example.com/media/carpet2.jpg",
            "isMain": false,
            "alt": "سجادة يدوية تراثية - تفاصيل النسج"
        }
    ],
    "attributes": [
        {
            "id": 45,
            "name": "الحجم",
            "type": "SELECT",
            "required": true,
            "orderIndex": 1,
            "values": [
                {
                    "id": 101,
                    "value": "صغير (120x80 سم)",
                    "extraPrice": 0.00,
                    "available": true
                },
                {
                    "id": 102,
                    "value": "متوسط (200x150 سم)",
                    "extraPrice": 50.00,
                    "available": true
                },
                {
                    "id": 103,
                    "value": "كبير (300x200 سم)",
                    "extraPrice": 120.00,
                    "available": false
                }
            ]
        }
    ],
    "pricing": {
        "basePrice": 250.00,
        "minPrice": 250.00,
        "maxPrice": 370.00,
        "currency": "ILS",
        "hasVariations": true
    },
    "ratings": {
        "average": 4.5,
        "count": 12,
        "distribution": {
            "5": 8,
            "4": 3,
            "3": 1,
            "2": 0,
            "1": 0
        }
    },
    "reviews": [
        {
            "id": 45,
            "customerName": "أحمد محمد",
            "rating": 5,
            "comment": "منتج ممتاز وجودة عالية",
            "createdAt": "2024-08-20T15:30:00Z",
            "verified": true
        }
    ],
    "relatedProducts": [
        {
            "id": 124,
            "name": "وسادة يدوية",
            "basePrice": 75.00,
            "imageUrl": "https://example.com/media/pillow.jpg"
        }
    ],
    "specifications": {
        "material": "صوف طبيعي 100%",
        "origin": "فلسطين",
        "craftingTime": "2-3 أسابيع",
        "care": "تنظيف جاف فقط"
    },
    "shipping": {
        "freeShippingThreshold": 200.00,
        "localDeliveryTime": "2-5 أيام",
        "internationalDeliveryTime": "7-14 يوم"
    },
    "seo": {
        "canonicalUrl": "https://example.com/products/123",
        "breadcrumbs": [
            {"name": "الرئيسية", "url": "/"},
            {"name": "منسوجات", "url": "/category/textiles"},
            {"name": "سجادة يدوية تراثية", "url": "/products/123"}
        ]
    },
    "availability": {
        "inStock": true,
        "stockLevel": "متوفر",
        "backorderAllowed": false,
        "estimatedRestockDate": null
    },
    "createdAt": "2024-08-26T10:30:00Z",
    "updatedAt": "2024-08-26T11:00:00Z"
}
```

### GET /api/categories
عرض التصنيفات

**Query Parameters:**
- `parent` (long): معرف التصنيف الأب
- `level` (int): مستوى التصنيف

**Response (200 OK):**
```json
{
    "categories": [
        {
            "id": 1,
            "name": "منتجات منزلية",
            "slug": "home-products",
            "description": "مجموعة من المنتجات المنزلية اليدوية",
            "imageUrl": "https://example.com/media/home-category.jpg",
            "productsCount": 25,
            "parentId": null,
            "orderIndex": 1,
            "children": [
                {
                    "id": 5,
                    "name": "منسوجات",
                    "slug": "textiles",
                    "productsCount": 12,
                    "parentId": 1
                }
            ]
        }
    ]
}
```

---

## Media Management APIs

### POST /api/media/upload
رفع ملف وسائط

**Authentication:** Required

**Request:**
```http
Content-Type: multipart/form-data

file: [binary file data]
description: "وصف الصورة"
tags: "منتج,سجادة,تراثي"
```

**Response (201 Created):**
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "url": "https://example.com/media/550e8400-e29b-41d4-a716-446655440001.jpg",
    "filename": "carpet-image.jpg",
    "originalFilename": "سجادة-تراثية.jpg",
    "contentType": "image/jpeg",
    "size": 1048576,
    "description": "وصف الصورة",
    "tags": ["منتج", "سجادة", "تراثي"],
    "uploadedAt": "2024-08-26T10:30:00Z",
    "uploadedBy": 1
}
```

### GET /api/media
عرض ملفات الوسائط

**Query Parameters:**
- `page` (int): رقم الصفحة
- `size` (int): عدد العناصر
- `type` (string): نوع الملف (`image`, `video`, `document`)
- `tag` (string): البحث بالعلامة

**Response (200 OK):**
```json
{
    "content": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440001",
            "url": "https://example.com/media/image.jpg",
            "filename": "image.jpg",
            "contentType": "image/jpeg",
            "size": 1048576,
            "thumbnailUrl": "https://example.com/media/thumbs/image_thumb.jpg",
            "uploadedAt": "2024-08-26T10:30:00Z"
        }
    ],
    "totalElements": 50,
    "totalPages": 3
}
```

### DELETE /api/media/{mediaId}
حذف ملف وسائط

**Response (204 No Content)**

---

## Error Handling

### تنسيق الأخطاء الموحد

جميع أخطاء API ترجع بالتنسيق التالي:

```json
{
    "error": "ERROR_CODE",
    "message": "رسالة الخطأ بالعربية",
    "details": {
        "field1": "تفاصيل الخطأ",
        "field2": "تفاصيل أخرى"
    },
    "timestamp": "2024-08-26T10:30:00Z",
    "path": "/api/endpoint",
    "requestId": "req_12345678"
}
```

### أكواد الأخطاء الشائعة

| Code | HTTP Status | Description |
|------|-------------|-------------|
| VALIDATION_ERROR | 400 | خطأ في التحقق من البيانات |
| UNAUTHORIZED | 401 | غير مصرح للوصول |
| FORBIDDEN | 403 | ممنوع من الوصول |
| NOT_FOUND | 404 | المورد غير موجود |
| CONFLICT | 409 | تعارض في البيانات |
| OUT_OF_STOCK | 400 | المنتج غير متوفر |
| INSUFFICIENT_FUNDS | 400 | رصيد غير كافي |
| INVALID_PAYMENT | 400 | بيانات دفع غير صحيحة |
| ORDER_NOT_FOUND | 404 | الطلب غير موجود |
| CART_EMPTY | 400 | السلة فارغة |

### أمثلة على الأخطاء

**خطأ تحقق من البيانات:**
```json
{
    "error": "VALIDATION_ERROR",
    "message": "البيانات المدخلة غير صحيحة",
    "details": {
        "email": "صيغة الإيميل غير صحيحة",
        "password": "كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل",
        "quantity": "الكمية يجب أن تكون رقم موجب"
    },
    "timestamp": "2024-08-26T10:30:00Z",
    "path": "/authcontroller/register",
    "requestId": "req_12345678"
}
```

**خطأ عدم توفر المنتج:**
```json
{
    "error": "OUT_OF_STOCK",
    "message": "الكمية المطلوبة غير متوفرة في المخزون",
    "details": {
        "productId": 123,
        "productName": "سجادة يدوية تراثية",
        "requestedQuantity": 10,
        "availableQuantity": 3
    },
    "timestamp": "2024-08-26T10:30:00Z",
    "path": "/api/cart/1/add",
    "requestId": "req_87654321"
}
```

---

## Rate Limiting

### حدود الاستخدام

| Endpoint Type | Rate Limit | Window |
|---------------|------------|---------|
| Authentication | 5 requests | 1 minute |
| Product Browsing | 100 requests | 1 minute |
| Cart Operations | 30 requests | 1 minute |
| Order Operations | 10 requests | 1 minute |
| Media Upload | 20 uploads | 1 hour |

### Headers الاستجابة

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1693009800
```

### استجابة تجاوز الحد

```json
{
    "error": "RATE_LIMIT_EXCEEDED",
    "message": "تم تجاوز الحد المسموح من الطلبات",
    "details": {
        "limit": 100,
        "windowSeconds": 60,
        "retryAfter": 45
    },
    "timestamp": "2024-08-26T10:30:00Z"
}
```

---

## WebSocket APIs (مخطط مستقبلي)

### Order Status Updates
```javascript
// الاتصال
const socket = new WebSocket('ws://localhost:8080/ws/orders');

// الاشتراك في تحديثات الطلب
socket.send(JSON.stringify({
    type: 'SUBSCRIBE_ORDER',
    orderId: 567
}));

// استقبال التحديثات
socket.onmessage = function(event) {
    const update = JSON.parse(event.data);
    if (update.type === 'ORDER_STATUS_CHANGED') {
        console.log('Order status:', update.newStatus);
    }
};
```

### Real-time Notifications
```javascript
// إشعارات للشركات
socket.send(JSON.stringify({
    type: 'SUBSCRIBE_COMPANY_NOTIFICATIONS',
    companyId: 1
}));

// إشعار طلب جديد
{
    "type": "NEW_ORDER",
    "orderId": 568,
    "customerName": "أحمد محمد",
    "totalAmount": 450.00,
    "timestamp": "2024-08-26T10:30:00Z"
}
```

---

## تحسينات الأداء

### Caching Strategy
- **Product Catalog**: cache لمدة 1 ساعة
- **Company Pages**: cache لمدة 30 دقيقة  
- **User Sessions**: cache في الذاكرة
- **Static Content**: CDN caching

### Database Optimization
- فهارس محسنة للاستعلامات الشائعة
- Connection pooling
- Lazy loading للعلاقات
- Pagination للنتائج الكبيرة

### API Response Optimization
- Gzip compression
- JSON minification
- Selective field loading
- Batch operations

---

*تم إعداد هذا التوثيق كجزء من مشروع التجارة الإلكترونية للمنتجات المصنوعة يدوياً*  
*إعداد: محمد الحجين | التاريخ: أغسطس 2024*  
*إصدار API: v1.0*
