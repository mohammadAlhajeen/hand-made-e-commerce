package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.OrderItem;
import com.hand.demo.model.repository.OrderItemRepository;

@Service
public class OrderItemService {
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    public List<OrderItem> findAll() {
        return orderItemRepository.findAll();
    }
    
    public Optional<OrderItem> findById(Long id) {
        return orderItemRepository.findById(id);
    }
    
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
    
    public void deleteById(Long id) {
        orderItemRepository.deleteById(id);
    }
}
