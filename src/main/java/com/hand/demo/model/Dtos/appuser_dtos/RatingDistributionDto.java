package com.hand.demo.model.Dtos.appuser_dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RatingDistributionDto {
    private long one;
    private long two;
    private long three;
    private long four;
    private long five;
    private long count;        // ratingCount
    private BigDecimal average;    // convenience
}
