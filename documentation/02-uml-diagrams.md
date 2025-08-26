# Chapter 3: System Design and Architecture - UML Diagrams

## 3.1 System Design Overview

This chapter presents the comprehensive system design through various UML diagrams that illustrate the structure, behavior, and interactions within the Hand-Made E-Commerce System. The diagrams follow UML 2.5 standards and provide detailed insights into the system architecture.

## 3.2 Use Case Diagram

### 3.2.1 System Actors and Use Cases

```plantuml
@startuml
!theme plain
title Hand-Made E-Commerce System - Complete Use Case Analysis

left to right direction

actor "Company/Artisan" as Company
actor "Customer" as Customer  
actor "Driver" as Driver
actor "System Admin" as Admin

rectangle "E-Commerce System" {
  
  ' Company Use Cases
  usecase "Register Company" as UC1
  usecase "User Login" as UC2
  usecase "Manage Profile" as UC3
  usecase "Add New Product" as UC4
  usecase "Update Product" as UC5
  usecase "Delete Product" as UC6
  usecase "Manage Inventory" as UC7
  usecase "Manage Attributes & Values" as UC8
  usecase "Manage Orders" as UC9
  usecase "Confirm Deposit Receipt" as UC10
  usecase "Create Shipment" as UC11
  usecase "Manage Company Page" as UC12
  usecase "Upload Media" as UC13
  
  ' Customer Use Cases
  usecase "Register Customer" as UC14
  usecase "Browse Products" as UC15
  usecase "Search & Filter" as UC16
  usecase "View Product Details" as UC17
  usecase "Add to Cart" as UC18
  usecase "Manage Cart" as UC19
  usecase "Complete Order" as UC20
  usecase "Pay Deposit" as UC21
  usecase "Track Order" as UC22
  usecase "Write Review" as UC23
  usecase "View Company Page" as UC24
  
  ' Driver Use Cases
  usecase "Register Driver" as UC25
  usecase "Accept Delivery Tasks" as UC26
  usecase "Update Delivery Status" as UC27
  
  ' Admin Use Cases
  usecase "Manage Users" as UC28
  usecase "Manage Categories" as UC29
  usecase "Monitor System" as UC30
  usecase "Manage Reports" as UC31
}

' Company relationships
Company --> UC1
Company --> UC2
Company --> UC3
Company --> UC4
Company --> UC5
Company --> UC6
Company --> UC7
Company --> UC8
Company --> UC9
Company --> UC10
Company --> UC11
Company --> UC12
Company --> UC13

' Customer relationships
Customer --> UC14
Customer --> UC2
Customer --> UC3
Customer --> UC15
Customer --> UC16
Customer --> UC17
Customer --> UC18
Customer --> UC19
Customer --> UC20
Customer --> UC21
Customer --> UC22
Customer --> UC23
Customer --> UC24

' Driver relationships
Driver --> UC25
Driver --> UC2
Driver --> UC26
Driver --> UC27

' Admin relationships
Admin --> UC28
Admin --> UC29
Admin --> UC30
Admin --> UC31

' Include relationships
UC4 ..> UC13 : <<include>>
UC5 ..> UC13 : <<include>>
UC18 ..> UC17 : <<include>>
UC20 ..> UC19 : <<include>>
UC21 ..> UC20 : <<include>>

' Extend relationships
UC16 ..> UC15 : <<extend>>
UC23 ..> UC22 : <<extend>>

@enduml
```

## 2. Class Diagram - Core Entities

```plantuml
@startuml
!theme plain
title Class Diagram - Core Entities

package "User Management" {
  abstract class AppUser {
    -Long id
    -String username
    -String email
    -String password
    -String firstName
    -String lastName
    -String phone
    -Instant createdAt
    -Instant updatedAt
    +authenticate()
    +updateProfile()
  }
  
  class Company {
    -String companyName
    -String description
    -String businessLicense
    -String taxNumber
    -Boolean isVerified
    +createProduct()
    +manageOrders()
  }
  
  class Customer {
    -Date birthDate
    -String preferences
    +placeOrder()
    +writeReview()
  }
  
  class Driver {
    -String licenseNumber
    -String vehicleInfo
    -Boolean isAvailable
    +acceptDelivery()
    +updateStatus()
  }
}

package "Product Management" {
  abstract class Product {
    -Long id
    -String name
    -String description
    -BigDecimal basePrice
    -Long companyId
    -Long categoryId
    -Instant createdAt
    +calculatePrice()
    +isAvailable()
  }
  
  class InStockProduct {
    -Integer quantityInStock
    -Integer minStockLevel
    +checkStock()
    +reserveStock()
  }
  
  class PreOrderProduct {
    -Integer estimatedDays
    -BigDecimal depositPercentage
    -Integer maxPreOrders
    +calculateDeposit()
    +canAcceptPreOrder()
  }
  
  class Attribute {
    -Long id
    -String name
    -AttributeType type
    -Boolean required
    -Integer orderIndex
  }
  
  class AttributeValue {
    -Long id
    -String value
    -BigDecimal extraPrice
  }
  
  enum AttributeType {
    TEXT
    NUMBER
    SELECT
    MULTI_SELECT
    BOOLEAN
  }
  
  class Category {
    -Long id
    -String name
    -String description
    -Long parentId
    -String slug
  }
}

package "Cart and Orders" {
  class Cart {
    -Long id
    -Long customerId
    -Long companyId
    -Instant createdAt
    -Instant updatedAt
    +addItem()
    +removeItem()
    +calculateTotal()
  }
  
  class CartItem {
    -Long id
    -Integer quantity
    -BigDecimal unitPrice
    -BigDecimal extraPrice
    -Boolean depositRequired
    -BigDecimal depositAmount
    +calculateItemTotal()
    +getSelections()
  }
  
  class CartItemSelection {
    -Long id
    -Long attributeId
    -List<Long> selectedValueIds
  }
  
  class Order {
    -Long id
    -String orderNumber
    -OrderStatus status
    -BigDecimal totalAmount
    -BigDecimal depositAmount
    -BigDecimal depositPaid
    -Instant createdAt
    +processPayment()
    +updateStatus()
  }
  
  class OrderItem {
    -Long id
    -Integer quantity
    -BigDecimal unitPrice
    -BigDecimal extraPrice
    -Boolean depositRequired
    -BigDecimal depositAmount
    -Integer allocatedQuantity
    -Integer shippedQuantity
  }
  
  enum OrderStatus {
    PENDING_DEPOSIT
    DEPOSIT_PAID
    CONFIRMED
    PROCESSING
    PARTIALLY_SHIPPED
    SHIPPED
    DELIVERED
    CANCELLED
  }
}

package "Shipping" {
  class Shipment {
    -Long id
    -Long orderId
    -String trackingNumber
    -ShipmentStatus status
    -String shippingAddress
    -Instant shippedAt
    -Instant deliveredAt
    +updateStatus()
    +calculateShippingCost()
  }
  
  class ShipmentItem {
    -Long id
    -Long orderItemId
    -Integer quantity
  }
  
  enum ShipmentStatus {
    PENDING
    SHIPPED
    IN_TRANSIT
    DELIVERED
    RETURNED
  }
}

' Inheritance relationships
AppUser <|-- Company
AppUser <|-- Customer
AppUser <|-- Driver

Product <|-- InStockProduct
Product <|-- PreOrderProduct

' Associations
Company ||--o{ Product : creates
Product }o--|| Category : belongs_to
Product ||--o{ Attribute : has
Attribute ||--o{ AttributeValue : contains

Customer ||--o{ Cart : owns
Company ||--o{ Cart : sells_to
Cart ||--o{ CartItem : contains
CartItem ||--o{ CartItemSelection : has
CartItem }o--|| Product : references

Customer ||--o{ Order : places
Order ||--o{ OrderItem : contains
OrderItem }o--|| Product : references
OrderItem ||--o{ OrderItemSelection : has

Order ||--o{ Shipment : generates
Shipment ||--o{ ShipmentItem : contains
ShipmentItem }o--|| OrderItem : ships

@enduml
```

## 3. Sequence Diagram - Add to Cart Process

```plantuml
@startuml
!theme plain
title Add to Cart Process Sequence

actor Customer as C
participant "CartController" as CC
participant "CartService" as CS
participant "ProductRepository" as PR
participant "CartRepository" as CR
participant "AttributeValueRepository" as AVR
database "PostgreSQL" as DB

C -> CC: POST /api/cart/{companyId}/add
activate CC

CC -> CS: addItem(companyId, request)
activate CS

CS -> CR: findByCustomerIdAndCompanyId()
activate CR
CR -> DB: SELECT cart WHERE customer_id=? AND company_id=?
DB --> CR: Cart or null
CR --> CS: Cart or create new
deactivate CR

CS -> PR: findById(productId)
activate PR
PR -> DB: SELECT product WHERE id=?
DB --> PR: Product
PR --> CS: Product
deactivate PR

alt if selections provided
  CS -> AVR: sumExtra(selectedValueIds)
  activate AVR
  AVR -> DB: SELECT SUM(extra_price) FROM attribute_values WHERE id IN (?)
  DB --> AVR: BigDecimal extraPrice
  AVR --> CS: extraPrice
  deactivate AVR
end

CS -> CS: calculatePrices()
note right: Calculate unit price + extra price\nCalculate deposit if pre-order

CS -> CS: buildCartItem()
note right: Create CartItem with selections

CS -> CR: save(cartItem)
activate CR
CR -> DB: INSERT/UPDATE cart_item
DB --> CR: saved CartItem
CR --> CS: CartItem
deactivate CR

CS --> CC: CartItem
deactivate CS

CC -> CS: getCartView(companyId)
activate CS
CS -> CR: findWithDetails(customerId, companyId)
activate CR
CR -> DB: Complex JOIN query for cart details
DB --> CR: CartViewDTO data
CR --> CS: CartViewDTO
deactivate CR
CS --> CC: CartViewDTO
deactivate CS

CC --> C: 200 OK with CartViewDTO
deactivate CC

@enduml
```

## 4. Sequence Diagram - Order Checkout Process

```plantuml
@startuml
!theme plain
title Order Checkout Process Sequence

actor Customer as C
participant "OrderController" as OC
participant "OrderService" as OS
participant "CartService" as CS
participant "ProductService" as PS
participant "OrderRepository" as OR
database "PostgreSQL" as DB

C -> OC: POST /api/orders/checkout/{cartId}
activate OC

OC -> OS: checkout(cartId, customerId)
activate OS

OS -> CS: getCart(cartId)
activate CS
CS -> DB: SELECT cart with items
DB --> CS: Cart with items
CS --> OS: Cart
deactivate CS

loop for each cart item
  OS -> PS: validateAndReserve(productId, quantity)
  activate PS
  
  alt if InStockProduct
    PS -> DB: UPDATE stock quantity
    DB --> PS: success/failure
  else if PreOrderProduct
    PS -> DB: CHECK pre-order limits
    DB --> PS: availability
  end
  
  PS --> OS: validation result
  deactivate PS
end

OS -> OS: calculateOrderTotals()
note right: Calculate total amount,\ndeposit amount, etc.

OS -> OS: createOrder()
note right: Create Order entity with\nall items and selections

OS -> OR: save(order)
activate OR
OR -> DB: INSERT order and order_items
DB --> OR: saved Order
OR --> OS: Order
deactivate OR

alt if deposit required
  OS -> OS: setStatus(PENDING_DEPOSIT)
else
  OS -> OS: setStatus(CONFIRMED)
end

OS -> CS: clearCart(cartId)
activate CS
CS -> DB: DELETE cart_items
DB --> CS: success
CS --> OS: cleared
deactivate CS

OS --> OC: Order
deactivate OS

OC --> C: 200 OK with Order details
deactivate OC

@enduml
```

## 5. Component Diagram

```plantuml
@startuml
!theme plain
title Component Diagram - Architectural Structure

package "Presentation Layer" {
  [Web Controllers] as WC
  [REST APIs] as API
  [Authentication Filter] as AF
}

package "Business Layer" {
  [User Service] as US
  [Product Service] as PS
  [Cart Service] as CS
  [Order Service] as OS
  [Page Service] as PGS
  [Media Service] as MS
}

package "Data Access Layer" {
  [JPA Repositories] as REPO
  [Custom Queries] as CQ
  [Entity Managers] as EM
}

package "Security" {
  [JWT Service] as JWT
  [Password Encoder] as PE
  [Security Config] as SC
}

package "External Systems" {
  [File Storage] as FS
  [Email Service] as ES
  [Payment Gateway] as PG
}

database "PostgreSQL" as DB

cloud "Client Applications" as CLIENT

' Connections
CLIENT --> WC
WC --> API
API --> AF
AF --> JWT

API --> US
API --> PS  
API --> CS
API --> OS
API --> PGS

US --> REPO
PS --> REPO
CS --> REPO
OS --> REPO
PGS --> REPO

REPO --> EM
EM --> DB
CQ --> DB

US --> PE
AF --> SC
SC --> JWT

MS --> FS
OS --> ES
OS --> PG

' Dependencies
WC ..> CS : uses
WC ..> OS : uses
WC ..> PS : uses

CS ..> PS : validates products
OS ..> CS : processes cart
OS ..> PS : reserves stock

@enduml
```

## 6. Entity Relationship Diagram

```plantuml
@startuml
!theme plain
title مخطط علاقات قاعدة البيانات

entity "app_users" as users {
  * id : BIGINT <<PK>>
  --
  * username : VARCHAR(255) <<UK>>
  * email : VARCHAR(255) <<UK>>
  * password : VARCHAR(255)
  * first_name : VARCHAR(255)
  * last_name : VARCHAR(255)
  phone : VARCHAR(20)
  * created_at : TIMESTAMP
  updated_at : TIMESTAMP
  * user_type : VARCHAR(50)
}

entity "companies" as companies {
  * id : BIGINT <<PK,FK>>
  --
  * company_name : VARCHAR(255)
  description : TEXT
  business_license : VARCHAR(255)
  tax_number : VARCHAR(255)
  is_verified : BOOLEAN
}

entity "customers" as customers {
  * id : BIGINT <<PK,FK>>
  --
  birth_date : DATE
  preferences : TEXT
}

entity "categories" as categories {
  * id : BIGINT <<PK>>
  --
  * name : VARCHAR(255)
  description : TEXT
  parent_id : BIGINT <<FK>>
  * slug : VARCHAR(255) <<UK>>
  order_index : INTEGER
}

entity "products" as products {
  * id : BIGINT <<PK>>
  --
  * company_id : BIGINT <<FK>>
  * name : VARCHAR(255)
  description : TEXT
  * base_price : DECIMAL(10,2)
  category_id : BIGINT <<FK>>
  * product_type : VARCHAR(50)
  * created_at : TIMESTAMP
  updated_at : TIMESTAMP
}

entity "in_stock_products" as in_stock {
  * id : BIGINT <<PK,FK>>
  --
  * quantity_in_stock : INTEGER
  min_stock_level : INTEGER
}

entity "pre_order_products" as pre_order {
  * id : BIGINT <<PK,FK>>
  --
  estimated_days : INTEGER
  deposit_percentage : DECIMAL(5,2)
  max_pre_orders : INTEGER
}

entity "attributes" as attributes {
  * id : BIGINT <<PK>>
  --
  * product_id : BIGINT <<FK>>
  * name : VARCHAR(255)
  * type : VARCHAR(50)
  * required : BOOLEAN
  order_index : INTEGER
}

entity "attribute_values" as attr_values {
  * id : BIGINT <<PK>>
  --
  * attribute_id : BIGINT <<FK>>
  * value : VARCHAR(255)
  extra_price : DECIMAL(10,2)
}

entity "carts" as carts {
  * id : BIGINT <<PK>>
  --
  * customer_id : BIGINT <<FK>>
  * company_id : BIGINT <<FK>>
  * created_at : TIMESTAMP
  updated_at : TIMESTAMP
}

entity "cart_items" as cart_items {
  * id : BIGINT <<PK>>
  --
  * cart_id : BIGINT <<FK>>
  * product_id : BIGINT <<FK>>
  * quantity : INTEGER
  * unit_price : DECIMAL(10,2)
  extra_price : DECIMAL(10,2)
  deposit_required : BOOLEAN
  deposit_amount : DECIMAL(10,2)
  * created_at : TIMESTAMP
}

entity "cart_item_selections" as cart_selections {
  * id : BIGINT <<PK>>
  --
  * cart_item_id : BIGINT <<FK>>
  * attribute_id : BIGINT <<FK>>
  selected_value_ids : JSONB
}

entity "orders" as orders {
  * id : BIGINT <<PK>>
  --
  * customer_id : BIGINT <<FK>>
  * company_id : BIGINT <<FK>>
  * order_number : VARCHAR(50) <<UK>>
  * status : VARCHAR(50)
  * total_amount : DECIMAL(10,2)
  deposit_amount : DECIMAL(10,2)
  deposit_paid : DECIMAL(10,2)
  * created_at : TIMESTAMP
  updated_at : TIMESTAMP
}

entity "order_items" as order_items {
  * id : BIGINT <<PK>>
  --
  * order_id : BIGINT <<FK>>
  * product_id : BIGINT <<FK>>
  * quantity : INTEGER
  * unit_price : DECIMAL(10,2)
  extra_price : DECIMAL(10,2)
  deposit_required : BOOLEAN
  deposit_amount : DECIMAL(10,2)
  allocated_quantity : INTEGER
  shipped_quantity : INTEGER
}

entity "shipments" as shipments {
  * id : BIGINT <<PK>>
  --
  * order_id : BIGINT <<FK>>
  tracking_number : VARCHAR(255)
  * status : VARCHAR(50)
  shipping_address : TEXT
  shipped_at : TIMESTAMP
  delivered_at : TIMESTAMP
}

entity "shipment_items" as shipment_items {
  * id : BIGINT <<PK>>
  --
  * shipment_id : BIGINT <<FK>>
  * order_item_id : BIGINT <<FK>>
  * quantity : INTEGER
}

' Relationships
users ||--o{ companies : extends
users ||--o{ customers : extends

companies ||--o{ products : owns
categories ||--o{ products : categorizes
categories ||--o{ categories : parent/child

products ||--o{ in_stock : specializes
products ||--o{ pre_order : specializes

products ||--o{ attributes : has
attributes ||--o{ attr_values : contains

customers ||--o{ carts : owns
companies ||--o{ carts : sells_to
carts ||--o{ cart_items : contains
cart_items }o--|| products : references
cart_items ||--o{ cart_selections : has

customers ||--o{ orders : places
companies ||--o{ orders : receives
orders ||--o{ order_items : contains
order_items }o--|| products : references

orders ||--o{ shipments : generates
shipments ||--o{ shipment_items : contains
shipment_items }o--|| order_items : ships

@enduml
```

## 7. Deployment Diagram

```plantuml
@startuml
!theme plain
title مخطط النشر

node "Load Balancer" as LB {
  component [Nginx] as nginx
}

node "Application Server 1" as AS1 {
  component [Spring Boot App] as app1
  component [JVM] as jvm1
}

node "Application Server 2" as AS2 {
  component [Spring Boot App] as app2
  component [JVM] as jvm2
}

node "Database Server" as DB {
  component [PostgreSQL] as postgres
  database [handmade_ecommerce] as db
}

node "File Storage" as FS {
  component [File System] as filesystem
  storage [Media Files] as media
}

node "Monitoring" as MON {
  component [Prometheus] as prometheus
  component [Grafana] as grafana
}

cloud "Internet" as internet
actor "Users" as users

' Connections
users --> internet
internet --> LB : HTTPS
LB --> AS1 : HTTP
LB --> AS2 : HTTP

AS1 --> DB : JDBC
AS2 --> DB : JDBC

AS1 --> FS : File I/O
AS2 --> FS : File I/O

AS1 --> MON : Metrics
AS2 --> MON : Metrics
DB --> MON : Metrics

' Internal connections
nginx --> app1
nginx --> app2
app1 --> jvm1
app2 --> jvm2
postgres --> db
filesystem --> media
prometheus --> grafana

@enduml
```

---

## ملاحظات للرسوم البيانية

### متطلبات العرض
لعرض هذه الرسوم البيانية، ستحتاج إلى:

1. **PlantUML Plugin** في IDE مثل VS Code أو IntelliJ
2. **Online PlantUML Editor** على الرابط: [http://www.plantuml.com/plantuml/uml/](http://www.plantuml.com/plantuml/uml/)
3. **PlantUML Desktop Application**

### تخصيص الرسوم
يمكن تخصيص الرسوم البيانية من خلال:
- تغيير الألوان والثيمات
- إضافة تفاصيل أكثر للعمليات
- تقسيم الرسوم الكبيرة لأجزاء أصغر
- إضافة ملاحظات توضيحية

### التحديث المستمر
هذه الرسوم البيانية يجب تحديثها عند:
- إضافة ميزات جديدة
- تعديل البنية المعمارية
- تغيير في العلاقات بين الكيانات
- تطوير واجهات برمجة جديدة
