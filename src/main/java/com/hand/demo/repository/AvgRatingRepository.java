package com.hand.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.AvgRating;

@Repository
public interface AvgRatingRepository extends JpaRepository<AvgRating, Long> {
}
