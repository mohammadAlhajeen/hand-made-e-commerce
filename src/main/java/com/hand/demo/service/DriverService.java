package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.Driver;
import com.hand.demo.repository.DriverRepository;


@Service
public class DriverService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    public List<Driver> findAll() {
        return driverRepository.findAll();
    }
    
    public Optional<Driver> findById(Long id) {
        return driverRepository.findById(id);
    }
    
    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }
    
    public void deleteById(Long id) {
        driverRepository.deleteById(id);
    }
}
