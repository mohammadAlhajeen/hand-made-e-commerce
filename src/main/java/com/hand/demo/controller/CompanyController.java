/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.hand.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.security.auth.login.CredentialException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

import com.hand.demo.model.Dtos.appuser_dtos.UpdateCompanyDto;
import com.hand.demo.model.Dtos.product_dtos.CreateInStockProductDto;
import com.hand.demo.model.Dtos.product_dtos.CreatePreOrderProductDto;
import com.hand.demo.model.Dtos.product_dtos.InStockProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.PreOrderProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.UpdateInStockProductDto;
import com.hand.demo.model.Dtos.product_dtos.UpdatePreOrderProductDto;
import com.hand.demo.repository.GetReviewsProjection;
import com.hand.demo.service.CompanyService;
import com.hand.demo.service.MediaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Mohammad
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
public class CompanyController {

    private final MediaService mediaService;

    private final CompanyService companyService;

    @PostMapping("/update")
    public ResponseEntity<?> companyUpdate(@RequestBody  UpdateCompanyDto company) throws CredentialException {

        try {
            return new ResponseEntity<>(companyService.updateCompany(company), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
      

    }
    // ############################
    // #### Product Operations ####
    // ############################

    // Create Pre-Order Product
    @PostMapping("/PreOrderProduct")
    public ResponseEntity<?> createPreOrderProduct(@RequestBody @Valid CreatePreOrderProductDto productDto)
            throws CredentialException {
        System.out.println(productDto);
   
            PreOrderProductForCompanyV1 product = companyService.createPreOrderProductDto(productDto);
            return ResponseEntity.ok(product);
   
      
        
    }

    // Create In-Stock Product
    @PostMapping("/InStockProduct")
    public ResponseEntity<?> createInStockProduct(@RequestBody @Valid CreateInStockProductDto productDto)
            throws CredentialException {
        System.out.println(productDto);
        
            InStockProductForCompanyV1 product = companyService.createInStockProductDto(productDto);
            System.out.println(product.toString());
            return ResponseEntity.ok(product);
      
    }

    // Update Product
    @PutMapping("/PreOrderProduct/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestBody @Valid UpdatePreOrderProductDto productDto) throws CredentialException {
        // TODO
        PreOrderProductForCompanyV1 updatedProduct = companyService.updateMyProduct(productDto, productId);


            return ResponseEntity.ok(updatedProduct);
        
    }

    @PutMapping("/InStockProduct/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateInStockProductDto productDto) throws CredentialException {
        // TODO
        InStockProductForCompanyV1 updatedProduct = companyService.updateMyProduct(productDto, productId);

 
            return ResponseEntity.ok(updatedProduct);
        
    }

    // جلب منتج معين
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Long productId) throws CredentialException {

        var product = companyService.getCompanyProduct(productId);

        return ResponseEntity.ok(product);

    }

    // جلب قائمة المنتجات الخاصة بالشركة
    @GetMapping("/products")
    public ResponseEntity<List<?>> listProducts() throws CredentialException {
        List<?> products = companyService.listMyProducts();
        return ResponseEntity.ok(products);
    }

    // Active Product
    @PatchMapping("/product/{productId}/activate")
    public ResponseEntity<Void> activateProduct(
            @PathVariable Long productId,
            @RequestParam boolean active) throws CredentialException {
        companyService.activateMyProduct(productId, active);
        return ResponseEntity.noContent().build();
    }

    // Delete Product
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

    @PostMapping(value ="/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addImage(@RequestPart("file") MultipartFile file)
            throws IOException, CredentialException {
        var imageUrl = companyService.addImg(file);
        return ResponseEntity.ok(imageUrl);
    }


    @DeleteMapping
    public  ResponseEntity<?> delete(@RequestParam("path") UUID pathOrUrl) throws IOException, CredentialException {
        companyService.removeImg(pathOrUrl);
        return ResponseEntity.ok("Image deleted successfully");
    }
}
