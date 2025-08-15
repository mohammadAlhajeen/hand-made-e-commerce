/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.hand.demo.controller;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.CredentialException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hand.demo.model.Dtos.UpdateCompanyDto;
import com.hand.demo.model.Dtos.product_dtos.CreateProductDto;
import com.hand.demo.model.Dtos.product_dtos.ProductForCompany;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.repository.GetReviewsProjection;
import com.hand.demo.service.CompanyService;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Mohammad
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/update")
    public ResponseEntity<?> companyUpdate(@ModelAttribute UpdateCompanyDto company,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            if (image != null) {
                return new ResponseEntity<>(companyService.updateCompany(company, image), HttpStatus.OK);
            }
            return new ResponseEntity<>(companyService.updateCompany(company), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    // ############################
    // #### Product Operations ####
    // ############################

    // إنشاء منتج جديد
    @PostMapping("/product")
    public ResponseEntity<?> createProduct(@RequestBody CreateProductDto productDto) throws CredentialException {
        System.out.println(productDto);
        try {
            ProductForCompany product = companyService.createProductDto(productDto);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // تحديث منتج
    @PutMapping("/product/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestBody CreateProductDto productDto) throws CredentialException {
        //TODO
                ProductForCompany updatedProduct = companyService.updateMyProduct(productDto, productId);
        System.out.println(updatedProduct);
        try {
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // جلب منتج معين
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Long productId) throws CredentialException {
        try {
            Product product = companyService.getCompanyProduct(productId);
            System.out.println(product);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            //TODO Just for testing
            System.out.println(e);
        }
        return ResponseEntity.badRequest().body("Product not found or you don't have permission to access it");
    }

    // جلب قائمة المنتجات الخاصة بالشركة
    @GetMapping("/products")
    public ResponseEntity<List<?>> listProducts() throws CredentialException {
        List<?> products = companyService.listMyProducts();
        return ResponseEntity.ok(products);
    }

    // تفعيل أو تعطيل منتج
    @PatchMapping("/product/{productId}/activate")
    public ResponseEntity<Void> activateProduct(
            @PathVariable Long productId,
            @RequestParam boolean active) throws CredentialException {
        companyService.activateMyProduct(productId, active);
        return ResponseEntity.noContent().build();
    }

    // حذف منتج
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) throws CredentialException {
        var response = companyService.deleteMyProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}/reviews/ascrating")
    public ResponseEntity<?> getProductReviewsAscRat(@PathVariable Long productId) throws CredentialException {
        List<GetReviewsProjection> reviews = companyService.getReviewAscRat(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}/reviews/desrating")
    public ResponseEntity<?> getProductReviewsDesRat(@PathVariable Long productId) throws CredentialException {
        List<GetReviewsProjection> reviews = companyService.getReviewDesRat(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}/reviews/descreate")
    public ResponseEntity<?> getProductReviewsDesCreate(@PathVariable Long productId) throws CredentialException {
        List<GetReviewsProjection> reviews = companyService.getReviewDesCreate(productId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/images")
    public ResponseEntity<String> addImage(@RequestParam("file") MultipartFile file)
            throws IOException, CredentialException {
        String imageUrl = companyService.addImg(file);
        return ResponseEntity.ok(imageUrl);
    }
}
