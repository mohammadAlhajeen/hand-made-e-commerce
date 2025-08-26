package com.hand.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.OrderItemSelection;

@Repository
public interface OrderItemSelectionRepository extends JpaRepository<OrderItemSelection, Long> {
}
