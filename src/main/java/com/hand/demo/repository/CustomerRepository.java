package com.hand.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public boolean existsByUsername(String username);
    public Optional<Customer> findByUsername(String username);

}
