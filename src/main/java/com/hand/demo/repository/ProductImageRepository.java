package com.hand.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

}