package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.AttributeValue;
import com.hand.demo.model.repository.AttributeValueRepository;

@Service
public class AttributeValueService {
    
    @Autowired
    private AttributeValueRepository attributeValueRepository;
    
    public List<AttributeValue> findAll() {
        return attributeValueRepository.findAll();
    }
    
    public Optional<AttributeValue> findById(Long id) {
        return attributeValueRepository.findById(id);
    }
    
    public AttributeValue save(AttributeValue attributeValue) {
        return attributeValueRepository.save(attributeValue);
    }
    
    public void deleteById(Long id) {
        attributeValueRepository.deleteById(id);
    }
}
