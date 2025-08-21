
package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.Attribute;
import com.hand.demo.repository.AttributeRepository;

@Service
public class AttributeService {
    
    @Autowired
    private AttributeRepository attributeRepository;
    
    public List<Attribute> findAll() {
        return attributeRepository.findAll();
    }
    
    public Optional<Attribute> findById(Long id) {
        return attributeRepository.findById(id);
    }
    
    public Attribute save(Attribute attribute) {
        return attributeRepository.save(attribute);
    }
    
    public void deleteById(Long id) {
        attributeRepository.deleteById(id);
    }
}
