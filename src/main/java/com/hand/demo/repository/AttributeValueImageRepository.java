package com.hand.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.AttributeValueImage;

@Repository
public interface AttributeValueImageRepository extends JpaRepository<AttributeValueImage, UUID> {

}