/*
 * Public Product Controller
 * Handles public product browsing, search, and display
 */
package com.hand.demo.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hand.demo.repository.GetProductCardProjection;
import com.hand.demo.service.CategoryService;
import com.hand.demo.service.ProductService;
import com.hand.demo.service.TagService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class PublicProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final TagService tagService;

    // Browse all products
    @GetMapping
    public ResponseEntity<Page<GetProductCardProjection>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search) {

        Page<GetProductCardProjection> products = productService.getPublicProducts(page, size, category, tag, search);
        return ResponseEntity.ok(products);
    }

    // Get product details
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getPublicProduct(productId));
    }

    // Search products
    @GetMapping("/search")
    public ResponseEntity<List<GetProductCardProjection>> searchProducts(@RequestParam String query) {
        List<GetProductCardProjection> products = productService.searchProducts(query);
        return ResponseEntity.ok(products);
    }

    // Get products by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<GetProductCardProjection>> getProductsByCategory(@PathVariable Long categoryId) {
        List<GetProductCardProjection> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    // Get featured products
    @GetMapping("/featured")
    public ResponseEntity<List<GetProductCardProjection>> getFeaturedProducts() {
        List<GetProductCardProjection> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    // Get all categories
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    @GetMapping("/categories/parents")
    public ResponseEntity<?> getParentCategories() {
        return ResponseEntity.ok(categoryService.getAllParentCategories());
    }

    @GetMapping("/categories/children/{parentId}")
    public ResponseEntity<?> getChildCategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getAllChildCategories(parentId));
    }

}
