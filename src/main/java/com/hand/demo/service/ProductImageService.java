package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.ProductImage;
import com.hand.demo.model.repository.ProductImageRepository;

@Service
public class ProductImageService {
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    public List<ProductImage> findAll() {
        return productImageRepository.findAll();
    }
    
    public Optional<ProductImage> findById(Long id) {
        return productImageRepository.findById(id);
    }
    
    public ProductImage save(ProductImage productImage) {
        return productImageRepository.save(productImage);
    }
    
    public void deleteById(Long id) {
        productImageRepository.deleteById(id);
    }
}