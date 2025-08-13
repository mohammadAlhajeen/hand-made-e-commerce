package com.hand.demo.model.repository;


public interface GetProductCardProjection {
    Long getId();
    String getName();
    String getDescription();
    java.math.BigDecimal getPrice();
    String getMainImageUrl();
    Integer getPreparationDays();
}
