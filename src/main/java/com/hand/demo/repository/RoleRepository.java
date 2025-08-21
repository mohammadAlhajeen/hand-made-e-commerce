/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.hand.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hand.demo.model.entity.Role;

/**
 *
 * @author Mohammad
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    
}
