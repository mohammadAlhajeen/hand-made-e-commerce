# Chapter 5: Implementation Details and Code Architecture

## 5.1 Implementation Overview

### 5.1.1 Development Methodology

The Hand-Made E-Commerce System was developed using Agile methodology with iterative development cycles. The implementation follows Test-Driven Development (TDD) principles and incorporates continuous integration practices.

**Development Phases:**
1. **Sprint 1 (Weeks 1-3)**: Core authentication and user management
2. **Sprint 2 (Weeks 4-6)**: Company and product management
3. **Sprint 3 (Weeks 7-9)**: Shopping cart functionality
4. **Sprint 4 (Weeks 10-12)**: Order processing and payment integration
5. **Sprint 5 (Weeks 13-15)**: Testing, optimization, and documentation
6. **Sprint 6 (Weeks 16-18)**: Deployment and final testing

### 5.1.2 Code Architecture Principles

The implementation adheres to several software engineering principles:

- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **Clean Architecture**: Separation of concerns with clear layer boundaries
- **Domain-Driven Design**: Rich domain models with business logic encapsulation
- **Dependency Injection**: Loose coupling through Spring's IoC container
- **Repository Pattern**: Data access abstraction layer

## 5.2 Package Structure and Organization

### 5.2.1 Project Structure

```
src/main/java/com/hand/
├── auth/                          # Authentication & Authorization
│   ├── authController.java        # Login/logout endpoints
│   ├── authLogin.java            # Login request handler
│   ├── AuthorizedUserService.java # User authorization service
│   └── AuthResponse.java         # Authentication response DTO
├── config/                        # Configuration classes
│   ├── AppConfiguration.java     # General application config
│   ├── jwtFilter.java           # JWT token filter
│   ├── jwtService.java          # JWT token management
│   └── SecurityConfig.java      # Spring Security configuration
├── controller/                    # REST API controllers
│   ├── CartController.java       # Shopping cart endpoints
│   ├── CompanyController.java    # Company management endpoints
│   ├── OrderController.java      # Order processing endpoints
│   └── ProductController.java    # Product management endpoints
├── model/                         # Data models and DTOs
│   ├── dto/                      # Data Transfer Objects
│   │   ├── AppUserDto.java       # User data transfer
│   │   ├── AppUserRegisterDTO.java # Registration data
│   │   ├── CartViewDTO.java      # Cart display data
│   │   ├── LogInRequest.java     # Login request data
│   │   └── UpdateCompanyDto.java # Company update data
│   ├── entity/                   # JPA entities
│   │   ├── AppUser.java          # User entity
│   │   ├── Company.java          # Company entity
│   │   ├── Product.java          # Product entity
│   │   ├── Cart.java             # Shopping cart entity
│   │   ├── Order.java            # Order entity
│   │   ├── Attribute.java        # Product attribute entity
│   │   └── AttributeValue.java   # Attribute value entity
│   └── repository/               # Data access layer
│       ├── AppUserRepository.java    # User data access
│       ├── CompanyRepository.java    # Company data access
│       ├── ProductRepository.java    # Product data access
│       ├── CartRepository.java       # Cart data access
│       └── OrderRepository.java      # Order data access
├── service/                       # Business logic layer
│   ├── AppUserService.java       # User business logic
│   ├── CompanyService.java       # Company business logic
│   ├── CartService.java          # Cart business logic
│   ├── OrderService.java         # Order business logic
│   └── ImageUrlService.java      # Media management service
└── demo/
    └── DemoApplication.java       # Spring Boot main class
```

## 5.3 Core Implementation Components

### 5.3.1 Authentication and Security

#### JWT Service Implementation

```java
@Service
public class jwtService {
    
    private final String JWT_SECRET = "your-secret-key";
    private final int JWT_EXPIRATION = 86400000; // 24 hours
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    // Additional methods for token extraction and validation
}
```

#### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private jwtFilter jwtRequestFilter;
    
    @Autowired
    private AuthorizedUserService userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/companies/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 5.3.2 Data Access Layer Implementation

#### Repository Pattern with Custom Queries

```java
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findByCustomerIdAndCompanyId(Long customerId, Long companyId);
    
    @Query("""
        SELECT new com.hand.model.dto.CartViewDTO(
            c.id,
            c.companyId,
            comp.name,
            COUNT(ci.id),
            COALESCE(SUM(ci.totalPrice), 0)
        )
        FROM Cart c
        LEFT JOIN CartItem ci ON c.id = ci.cart.id
        JOIN Company comp ON c.companyId = comp.id
        WHERE c.customerId = :customerId
        GROUP BY c.id, c.companyId, comp.name
        """)
    List<CartViewDTO> findCartViewsByCustomerId(@Param("customerId") Long customerId);
    
    @Query("""
        SELECT ci FROM CartItem ci
        JOIN FETCH ci.product p
        LEFT JOIN FETCH p.imageUrls
        WHERE ci.cart.id = :cartId
        ORDER BY ci.createdAt DESC
        """)
    List<CartItem> findCartItemsWithProductDetails(@Param("cartId") Long cartId);
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.customerId = :customerId AND ci.cart.companyId = :companyId")
    int clearCartByCustomerAndCompany(@Param("customerId") Long customerId, 
                                     @Param("companyId") Long companyId);
}
```

#### Attribute Value Repository with Sum Calculation

```java
@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    
    List<AttributeValue> findByAttributeIdOrderBySortOrder(Long attributeId);
    
    @Query("SELECT SUM(av.extraPrice) FROM AttributeValue av WHERE av.id IN :ids")
    BigDecimal sumExtraPriceByIds(@Param("ids") List<Long> ids);
    
    @Query("""
        SELECT av FROM AttributeValue av
        JOIN FETCH av.attribute a
        WHERE av.id IN :ids
        ORDER BY a.sortOrder, av.sortOrder
        """)
    List<AttributeValue> findByIdsWithAttributes(@Param("ids") List<Long> ids);
}
```

### 5.3.3 Business Logic Layer

#### Cart Service Implementation

```java
@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AttributeValueRepository attributeValueRepository;
    
    public CartItem addItem(Long companyId, AddToCartRequest request) {
        // Validate customer authentication
        Long customerId = getCurrentUserId();
        
        // Find or create cart
        Cart cart = cartRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseGet(() -> createNewCart(customerId, companyId));
        
        // Validate product exists and belongs to company
        Product product = productRepository.findByIdAndCompanyId(
                request.getProductId(), companyId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        
        // Calculate pricing with attribute selections
        BigDecimal unitPrice = product.getBasePrice();
        BigDecimal extraPrice = BigDecimal.ZERO;
        
        if (request.getSelectedAttributeValues() != null && !request.getSelectedAttributeValues().isEmpty()) {
            extraPrice = attributeValueRepository
                .sumExtraPriceByIds(request.getSelectedAttributeValues());
        }
        
        BigDecimal totalItemPrice = unitPrice.add(extraPrice)
                .multiply(BigDecimal.valueOf(request.getQuantity()));
        
        // Check for existing item with same selections
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(request.getProductId()) &&
                           Objects.equals(item.getSelections(), request.getSelectedAttributeValues()))
            .findFirst();
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update existing item quantity
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setTotalPrice(cartItem.getUnitPrice().add(cartItem.getExtraPrice())
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .extraPrice(extraPrice)
                .totalPrice(totalItemPrice)
                .selections(request.getSelectedAttributeValues())
                .build();
            
            cart.getItems().add(cartItem);
        }
        
        // Validate stock for in-stock products
        if (product instanceof InStockProduct) {
            InStockProduct inStockProduct = (InStockProduct) product;
            if (cartItem.getQuantity() > inStockProduct.getQuantityInStock()) {
                throw new InsufficientStockException("Not enough stock available");
            }
        }
        
        cartRepository.save(cart);
        return cartItem;
    }
    
    public CartViewDTO getCartView(Long companyId) {
        Long customerId = getCurrentUserId();
        
        Cart cart = cartRepository.findByCustomerIdAndCompanyId(customerId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        
        List<CartItem> items = cartRepository.findCartItemsWithProductDetails(cart.getId());
        
        BigDecimal totalAmount = items.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartViewDTO.builder()
            .cartId(cart.getId())
            .companyId(companyId)
            .items(items.stream().map(this::mapToCartItemDTO).collect(Collectors.toList()))
            .totalAmount(totalAmount)
            .itemCount(items.size())
            .build();
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            // Implementation to get user ID from UserDetails
            return getUserIdFromUserDetails(userDetails);
        }
        throw new AuthenticationException("User not authenticated");
    }
}
```

### 5.3.4 Order Processing Implementation

#### Order Service with Deposit Handling

```java
@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductRepository productRepository;
    
    public Order processCheckout(Long cartId, CheckoutRequest request) {
        // Validate cart and get items
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout empty cart");
        }
        
        // Calculate order totals
        OrderCalculation calculation = calculateOrderTotals(cart);
        
        // Create order
        Order order = Order.builder()
            .customerId(cart.getCustomerId())
            .companyId(cart.getCompanyId())
            .orderNumber(generateOrderNumber())
            .subtotal(calculation.getSubtotal())
            .taxAmount(calculation.getTaxAmount())
            .shippingCost(calculation.getShippingCost())
            .totalAmount(calculation.getTotalAmount())
            .shippingAddress(request.getShippingAddress())
            .billingAddress(request.getBillingAddress())
            .notes(request.getNotes())
            .build();
        
        // Handle deposit requirements for pre-order items
        boolean hasPreOrderItems = cart.getItems().stream()
            .anyMatch(item -> item.getProduct() instanceof PreOrderProduct);
        
        if (hasPreOrderItems) {
            BigDecimal depositAmount = calculateDepositAmount(cart);
            order.setDepositRequired(true);
            order.setDepositAmount(depositAmount);
            order.setStatus(OrderStatus.PENDING_DEPOSIT);
        } else {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        
        // Create order items
        List<OrderItem> orderItems = cart.getItems().stream()
            .map(cartItem -> createOrderItem(order, cartItem))
            .collect(Collectors.toList());
        
        order.setItems(orderItems);
        
        // Reserve stock for in-stock products
        reserveStock(orderItems);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after successful order creation
        cartService.clearCart(cartId);
        
        return savedOrder;
    }
    
    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        return OrderItem.builder()
            .order(order)
            .product(cartItem.getProduct())
            .quantity(cartItem.getQuantity())
            .unitPrice(cartItem.getUnitPrice())
            .extraPrice(cartItem.getExtraPrice())
            .totalPrice(cartItem.getTotalPrice())
            .selections(cartItem.getSelections())
            .build();
    }
    
    private void reserveStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (item.getProduct() instanceof InStockProduct) {
                InStockProduct product = (InStockProduct) item.getProduct();
                int newStock = product.getQuantityInStock() - item.getQuantity();
                
                if (newStock < 0) {
                    throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName());
                }
                
                product.setQuantityInStock(newStock);
                productRepository.save(product);
            }
        }
    }
    
    @Async
    public void sendOrderConfirmationEmail(Long orderId) {
        // Asynchronous email sending implementation
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // Email service implementation
        emailService.sendOrderConfirmation(order);
    }
}
```

## 5.4 Error Handling and Validation

### 5.4.1 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException ex) {
        return ErrorResponse.builder()
            .error("ENTITY_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .build();
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInsufficientStock(InsufficientStockException ex) {
        return ErrorResponse.builder()
            .error("INSUFFICIENT_STOCK")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .build();
    }
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException ex) {
        return ErrorResponse.builder()
            .error("VALIDATION_ERROR")
            .message(ex.getMessage())
            .details(ex.getValidationErrors())
            .timestamp(Instant.now())
            .build();
    }
    
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthentication(AuthenticationException ex) {
        return ErrorResponse.builder()
            .error("AUTHENTICATION_FAILED")
            .message("Invalid credentials or expired token")
            .timestamp(Instant.now())
            .build();
    }
}
```

### 5.4.2 Input Validation

```java
@Data
@Builder
public class AddToCartRequest {
    
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;
    
    @Valid
    private List<@Positive(message = "Attribute value ID must be positive") Long> selectedAttributeValues;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
```

## 5.5 Performance Optimization Techniques

### 5.5.1 Database Query Optimization

```java
// Using @EntityGraph to reduce N+1 queries
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @EntityGraph(attributePaths = {"company", "category", "imageUrls", "attributes"})
    Page<Product> findByCompanyIdAndIsActiveTrue(Long companyId, Pageable pageable);
    
    @Query(value = """
        SELECT p.* FROM products p
        WHERE p.is_active = true
        AND (:companyId IS NULL OR p.company_id = :companyId)
        AND (:categoryId IS NULL OR p.category_id = :categoryId)
        AND (:minPrice IS NULL OR p.base_price >= :minPrice)
        AND (:maxPrice IS NULL OR p.base_price <= :maxPrice)
        AND (:searchTerm IS NULL OR p.search_vector @@ to_tsquery('english', :searchTerm))
        ORDER BY 
            CASE WHEN :searchTerm IS NOT NULL THEN ts_rank(p.search_vector, to_tsquery('english', :searchTerm)) END DESC,
            p.created_at DESC
        """, nativeQuery = true)
    Page<Product> findProductsWithFilters(
        @Param("companyId") Long companyId,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("searchTerm") String searchTerm,
        Pageable pageable);
}
```

### 5.5.2 Caching Implementation

```java
@Service
@CacheConfig(cacheNames = "productCache")
public class ProductService {
    
    @Cacheable(key = "#id")
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return mapToDTO(product);
    }
    
    @CacheEvict(key = "#result.id")
    public ProductDTO updateProduct(Long id, UpdateProductRequest request) {
        // Update implementation
        return updatedProduct;
    }
    
    @Caching(evict = {
        @CacheEvict(key = "#result.id"),
        @CacheEvict(cacheNames = "companyProductsCache", key = "#result.companyId")
    })
    public ProductDTO createProduct(CreateProductRequest request) {
        // Create implementation
        return newProduct;
    }
}
```

## 5.6 Testing Strategy and Implementation

### 5.6.1 Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    
    @Mock
    private CartRepository cartRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private AttributeValueRepository attributeValueRepository;
    
    @InjectMocks
    private CartService cartService;
    
    @Test
    @DisplayName("Should add item to cart successfully")
    void shouldAddItemToCartSuccessfully() {
        // Given
        Long customerId = 1L;
        Long companyId = 1L;
        Long productId = 1L;
        
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(2)
            .selectedAttributeValues(Arrays.asList(1L, 2L))
            .build();
        
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        cart.setCompanyId(companyId);
        
        Product product = InStockProduct.builder()
            .id(productId)
            .name("Test Product")
            .basePrice(BigDecimal.valueOf(50.00))
            .quantityInStock(10)
            .build();
        
        when(cartRepository.findByCustomerIdAndCompanyId(customerId, companyId))
            .thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndCompanyId(productId, companyId))
            .thenReturn(Optional.of(product));
        when(attributeValueRepository.sumExtraPriceByIds(anyList()))
            .thenReturn(BigDecimal.valueOf(10.00));
        when(cartRepository.save(any(Cart.class)))
            .thenReturn(cart);
        
        // When
        CartItem result = cartService.addItem(companyId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getUnitPrice()).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(result.getExtraPrice()).isEqualTo(BigDecimal.valueOf(10.00));
        assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(120.00));
        
        verify(cartRepository).save(cart);
    }
    
    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        Long companyId = 1L;
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(999L)
            .quantity(1)
            .build();
        
        when(productRepository.findByIdAndCompanyId(999L, companyId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> cartService.addItem(companyId, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Product not found");
    }
}
```

### 5.6.2 Integration Testing

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class CartControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Test
    @DisplayName("Should add item to cart via REST API")
    void shouldAddItemToCartViaRestAPI() {
        // Given
        Company company = createTestCompany();
        Product product = createTestProduct(company);
        String authToken = getAuthToken();
        
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(product.getId())
            .quantity(1)
            .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<AddToCartRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<CartItemDTO> response = restTemplate.exchange(
            "/api/cart/" + company.getId() + "/add",
            HttpMethod.POST,
            entity,
            CartItemDTO.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo(product.getId());
        assertThat(response.getBody().getQuantity()).isEqualTo(1);
        
        // Verify database state
        Optional<Cart> savedCart = cartRepository.findByCustomerIdAndCompanyId(
            getCurrentUserId(), company.getId());
        assertThat(savedCart).isPresent();
        assertThat(savedCart.get().getItems()).hasSize(1);
    }
}
```

## 5.7 Configuration and Deployment

### 5.7.1 Application Configuration

```yaml
# application.yml
spring:
  application:
    name: handmade-ecommerce
  
  datasource:
    url: jdbc:postgresql://localhost:5432/handmade_ecommerce
    username: ${DB_USERNAME:app_user}
    password: ${DB_PASSWORD:app_password}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        
  cache:
    type: redis
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      
  security:
    jwt:
      secret: ${JWT_SECRET:your-secret-key}
      expiration: ${JWT_EXPIRATION:86400000}

logging:
  level:
    com.hand: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 5.7.2 Docker Configuration

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/handmade-ecommerce-1.0.0.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
      - DB_USERNAME=app_user
      - DB_PASSWORD=app_password
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
    networks:
      - handmade-network

  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=handmade_ecommerce
      - POSTGRES_USER=app_user
      - POSTGRES_PASSWORD=app_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    networks:
      - handmade-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - handmade-network

volumes:
  postgres_data:

networks:
  handmade-network:
    driver: bridge
```
