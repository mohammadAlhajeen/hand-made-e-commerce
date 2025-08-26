package com.hand.demo.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
@Entity
@Table(name = "pre_order_products")
@DiscriminatorValue("PRE")
@PrimaryKeyJoinColumn(name = "id") // نفس PK تبع products
public class PreOrderProduct extends Product {

    @Column(name = "prepaid_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal prePaidPrice;

    @Column(name = "preparation_days", nullable = false)
    private Float preparationDays;

    // منطقي: العربون ≤ السعر الكلّي
    @AssertTrue(message = "Prepaid price must be >= 0 and <= full price")
    public boolean isDepositLEPrice() {
        return getPrice() != null
                && prePaidPrice != null
                && prePaidPrice.compareTo(BigDecimal.ZERO) >= 0
                && prePaidPrice.compareTo(getPrice()) <= 0;
    }

}
