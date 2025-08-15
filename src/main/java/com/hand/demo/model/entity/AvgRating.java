package com.hand.demo.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "avg_rating")
public class AvgRating implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private long fiveRating;
    private long fourRating;
    private long threeRating;
    private long twoRating;
    private long oneRating;
    private long totalRatings;
    private long ratingCount;
    @Column(name="average_rating")
    private BigDecimal averageRating=BigDecimal.ZERO;
    @Column(name="last_recomputed")
    private Date lastRecomputed;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true, name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;
    public AvgRating(Product product, BigDecimal averageRating, long fiveRating, long fourRating, long threeRating, long twoRating, long oneRating, long totalRatings) {
        this.product = product;
        this.averageRating = averageRating;
        this.fiveRating = fiveRating;
        this.fourRating = fourRating;
        this.threeRating = threeRating;
        this.twoRating = twoRating;
        this.oneRating = oneRating;
        this.totalRatings = totalRatings;
    }



}