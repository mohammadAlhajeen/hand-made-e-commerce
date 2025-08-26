/*
 * Customer Controller - Customer APIs
 * Handles customer registration, authentication, and shopping
 */
package com.hand.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hand.demo.model.Dtos.product_dtos.ProductDTOs;
import com.hand.demo.model.dto.AddToCartRequest;
import com.hand.demo.model.dto.CartViewDTO;
import com.hand.demo.model.dto.PayDepositRequest;
import com.hand.demo.model.dto.UpdateCartItemRequest;
import com.hand.demo.model.entity.Cart;
import com.hand.demo.model.entity.Order;
import com.hand.demo.model.enums.OrderStatus;
import com.hand.demo.service.CartService;
import com.hand.demo.service.OrderService;
import com.hand.demo.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;

    // ===== Product APIs =====
    
    // Get product details for customers
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDTOs> getProduct(@PathVariable Long productId) {
        ProductDTOs product = productService.getProductForCustomer(productId);
        return ResponseEntity.ok(product);
    }

    // ===== Cart Management APIs =====
    
    // Add item to cart
    @PostMapping("/{customerId}/cart/add")
    public ResponseEntity<Cart> addToCart(
            @PathVariable Long customerId,
            @RequestBody @Valid AddToCartRequest request) {
        
        Cart cart = cartService.addItemToCustomerCart(customerId, request.productId(), request);
        return ResponseEntity.ok(cart);
    }
    
    // Update cart item
    @PutMapping("/{customerId}/cart/{companyId}/update")
    public ResponseEntity<Cart> updateCartItem(
            @PathVariable Long customerId,
            @PathVariable Long companyId,
            @RequestBody @Valid UpdateCartItemRequest request) {
        
        Cart cart = cartService.updateCustomerCartItem(customerId, companyId, request);
        return ResponseEntity.ok(cart);
    }
    
    // Remove item from cart
    @DeleteMapping("/{customerId}/cart/{companyId}/remove/{cartItemId}")
    public ResponseEntity<Cart> removeFromCart(
            @PathVariable Long customerId,
            @PathVariable Long companyId,
            @PathVariable Long cartItemId) {
        
        Cart cart = cartService.removeItemFromCustomerCart(customerId, companyId, cartItemId);
        return ResponseEntity.ok(cart);
    }
    
    // Get customer cart for specific company
    @GetMapping("/{customerId}/cart/{companyId}")
    public ResponseEntity<CartViewDTO> getCart(
            @PathVariable Long customerId,
            @PathVariable Long companyId) {
        
        CartViewDTO cart = cartService.getCustomerCartView(customerId, companyId);
        return ResponseEntity.ok(cart);
    }
    
    // ===== Order Management APIs =====
    
    // Checkout cart
    @PostMapping("/{customerId}/orders/checkout/{cartId}")
    public ResponseEntity<Order> checkout(
            @PathVariable Long customerId,
            @PathVariable Long cartId) {
        
        Order order = orderService.customerCheckout(customerId, cartId);
        return ResponseEntity.ok(order);
    }
    
    // Pay deposit for order
    @PostMapping("/{customerId}/orders/pay-deposit")
    public ResponseEntity<Order> payDeposit(
            @PathVariable Long customerId,
            @RequestBody @Valid PayDepositRequest request) {
        
        Order order = orderService.customerPayDeposit(customerId, request);
        return ResponseEntity.ok(order);
    }
    
    // Get customer orders
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<List<Order>> getOrders(@PathVariable Long customerId, @RequestParam OrderStatus status) {
        List<Order> orders = orderService.getCustomerOrders(customerId, status);
        return ResponseEntity.ok(orders);
    }
    
    // Get specific order
    @GetMapping("/{customerId}/orders/{orderNumber}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long customerId,
            @PathVariable String orderNumber) {
        
        Order order = orderService.getCustomerOrder(customerId, orderNumber);
        return ResponseEntity.ok(order);
    }
    
}
