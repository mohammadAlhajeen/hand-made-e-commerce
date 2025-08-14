package com.hand.demo.model.repository;


import java.math.BigDecimal;

public interface GetProductCardProjection {
    Long getId();
    String getName();
    String getDescription();
    java.math.BigDecimal getPrice();
    String getMainImageUrl();
    Integer getPreparationDays();
    BigDecimal getAverageRating(); // الجديد
}
