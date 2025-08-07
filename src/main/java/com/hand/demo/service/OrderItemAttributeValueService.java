package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.OrderItemAttributeValue;
import com.hand.demo.model.repository.OrderItemAttributeValueRepository;

@Service
public class OrderItemAttributeValueService {
    
    @Autowired
    private OrderItemAttributeValueRepository orderItemAttributeValueRepository;
    
    public List<OrderItemAttributeValue> findAll() {
        return orderItemAttributeValueRepository.findAll();
    }
    
    public Optional<OrderItemAttributeValue> findById(Long id) {
        return orderItemAttributeValueRepository.findById(id);
    }
    
    public OrderItemAttributeValue save(OrderItemAttributeValue orderItemAttributeValue) {
        return orderItemAttributeValueRepository.save(orderItemAttributeValue);
    }
    
    public void deleteById(Long id) {
        orderItemAttributeValueRepository.deleteById(id);
    }
}
