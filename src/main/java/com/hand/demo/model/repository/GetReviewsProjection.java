package com.hand.demo.model.repository;

import java.time.LocalDateTime;

public interface GetReviewsProjection {
    Long getId();

    byte getRating();

    String getShortComment();

    LocalDateTime getCreatedAt();

    Long getProductId();

    Long getCustomerId();

    String getCustomerName();
}
