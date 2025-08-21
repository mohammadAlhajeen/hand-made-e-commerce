package com.hand.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.AttributeValueImage;
import com.hand.demo.repository.AttributeValueImageRepository;

@Service
public class AttributeValueImageService {
    
    @Autowired
    private AttributeValueImageRepository attributeValueImageRepository;
    
    public List<AttributeValueImage> findAll() {
        return attributeValueImageRepository.findAll();
    }
    
    public Optional<AttributeValueImage> findById(UUID id) {
        return attributeValueImageRepository.findById(id);
    }
    
    public AttributeValueImage save(AttributeValueImage attributeValueImage) {
        return attributeValueImageRepository.save(attributeValueImage);
    }
    
    public void deleteById(UUID id) {
        attributeValueImageRepository.deleteById(id);
    }
}