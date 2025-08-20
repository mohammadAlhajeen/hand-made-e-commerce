package com.hand.demo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "in_stock_products")
@DiscriminatorValue("STOCK")
@PrimaryKeyJoinColumn(name = "id") // نفس PK تبع products
public class InStockProduct extends Product {

    @Column(nullable = false)
    private Integer quantity = 0;

    // سياسة الإرجاع للمخزون
    @Column(name = "returnable", nullable = false)
    private boolean returnable = true;

    @Column(name = "return_days")
    private Integer returnDays = 14;

    // أوضح من orderUnderQuantity
    @Column(name = "allow_backorder", nullable = false)
    private boolean allowBackorder = false;

    @AssertTrue(message = "If not returnable, returnDays must be null; if returnable, returnDays must be >= 0")
    public boolean isReturnPolicyValid() {
        return returnable ? (returnDays != null && returnDays >= 0)
                : (returnDays == null);
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        // لو غير قابل للإرجاع، تأكد من تفريغ returnDays
        if (!returnable) {
            returnDays = null;
        }
        // احذف القيم السالبة بالخطأ
        if (quantity == null || quantity < 0) {
            quantity = 0;
        }
    }
}
