package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.Customer;
import com.hand.demo.model.repository.CustomerRepository;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
    
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }
    
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
    
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }
}
