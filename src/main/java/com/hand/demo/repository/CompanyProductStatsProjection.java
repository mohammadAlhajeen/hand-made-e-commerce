package com.hand.demo.repository;

public interface CompanyProductStatsProjection {
    Long getTotalProducts();
    Long getActiveProducts();
    Double getAvgRating();
}
