package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.OrderItemSelection;
import com.hand.demo.repository.OrderItemSelectionRepository;

@Service
public class OrderItemSelectionService {
    
    @Autowired
    private OrderItemSelectionRepository orderItemSelectionRepository;
    
    public List<OrderItemSelection> findAll() {
        return orderItemSelectionRepository.findAll();
    }
    
    public Optional<OrderItemSelection> findById(Long id) {
        return orderItemSelectionRepository.findById(id);
    }
    
    public OrderItemSelection save(OrderItemSelection orderItemSelection) {
        return orderItemSelectionRepository.save(orderItemSelection);
    }
    
    public void deleteById(Long id) {
        orderItemSelectionRepository.deleteById(id);
    }
}
