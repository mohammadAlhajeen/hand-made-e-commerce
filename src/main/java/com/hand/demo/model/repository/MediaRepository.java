package com.hand.demo.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hand.demo.model.entity.MediaItem;

import jakarta.persistence.LockModeType;



public interface MediaRepository extends JpaRepository<MediaItem, UUID> {

  java.util.Optional<MediaItem> findByPublicPath(String publicPath);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from MediaItem u where u.publicPath = :path")
  java.util.Optional<MediaItem> findForUpdate(@Param("path") String publicPath);

  List<MediaItem> findByUserId(Long userId);

  Optional<MediaItem> findByIdAndUserId(UUID mediaId, Long userId);
}