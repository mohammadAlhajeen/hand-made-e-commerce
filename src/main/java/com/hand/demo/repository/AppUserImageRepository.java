package com.hand.demo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.AppUser;
import com.hand.demo.model.entity.AppUserImage;

@Repository
public interface AppUserImageRepository extends JpaRepository<AppUserImage, UUID> {
    Optional<AppUserImage> findByAppUser(AppUser appUser);
}