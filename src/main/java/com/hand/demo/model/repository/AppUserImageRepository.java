package com.hand.demo.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.AppUserImage;

@Repository
public interface AppUserImageRepository extends JpaRepository<AppUserImage, Long> {

}