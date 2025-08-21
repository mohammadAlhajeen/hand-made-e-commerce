package com.hand.demo.repository;

import java.math.BigDecimal;

public interface GetProductCardProjection {
    Long getId();

    String getName();

    BigDecimal getPrice();

    String getMainImageUrl();

    Integer getPreparationDays();

    BigDecimal getAverageRating(); // الجديد
}
