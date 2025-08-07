/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.hand.demo.model.repository;

import com.hand.demo.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Mohammad
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
    
}
