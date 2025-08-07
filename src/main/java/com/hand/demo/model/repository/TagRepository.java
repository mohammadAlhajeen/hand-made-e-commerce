package com.hand.demo.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}
