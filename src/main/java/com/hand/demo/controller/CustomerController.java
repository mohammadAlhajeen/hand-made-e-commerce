/*
 * Customer Controller - Customer APIs
 * Handles customer registration, authentication, and shopping
 */
package com.hand.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hand.demo.model.Dtos.product_dtos.ProductDTOs;
import com.hand.demo.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final ProductService productService;

    // Get product details for customers
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDTOs> getProduct(@PathVariable Long productId) {
        ProductDTOs product = productService.getProductForCustomer(productId);
        return ResponseEntity.ok(product);
    }

    // TODO: سيتم إضافة المزيد من Customer APIs لاحقاً
    // - Cart Management
    // - Order Management 
    // - Reviews and Ratings
    
}
