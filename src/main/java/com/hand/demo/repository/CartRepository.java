package com.hand.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerIdAndCompanyIdAndClosedFalse(Long customerId, Long companyId);
    List<Cart> findByCustomerIdAndClosedFalse(Long customerId);
}
