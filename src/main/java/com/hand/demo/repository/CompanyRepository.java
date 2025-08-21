/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.hand.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hand.demo.model.entity.Company;

/**
 *
 * @author Mohammad
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {
    public boolean existsByUsername(String username);
    public Optional<Company> findByUsername(String username);

}
