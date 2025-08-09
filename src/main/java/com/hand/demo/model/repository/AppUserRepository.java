/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.hand.demo.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hand.demo.model.entity.AppUser;

/**
 *
 * @author Mohammad
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @Query("SELECT u FROM AppUser u WHERE u.deleted=false AND u.username = :username")
    Optional<AppUser> findByUsername(@Param("username") String username);

    public boolean existsByUsername(String username);

    @Query("""
            SELECT u.username AS username,
                u.password AS password,
                u.enabled AS enabled,
                u.accountNonExpired AS accountNonExpired,
                u.credentialsNonExpired AS credentialsNonExpired,
                u.accountNonLocked AS accountNonLocked,
                r.name AS roleName
            FROM AppUser u
            JOIN u.roles r
            WHERE u.deleted = false AND u.username = :username
            """)
    List<AppUserProjection> findUserWithRoles(@Param("username") String username);

}
