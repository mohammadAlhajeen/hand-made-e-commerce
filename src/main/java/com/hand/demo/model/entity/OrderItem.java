package com.hand.demo.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hand.demo.model.enums.OrderItemType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    @JsonManagedReference
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderItemType type;

    @Column(name = "unit_price_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceBase;

    @Column(name = "unit_price_extra", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceExtra = BigDecimal.ZERO;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO; // مبلغ العربون للوحدة الواحدة

    @Min(1)
    @Column(name = "qty_ordered", nullable = false)
    private Integer qtyOrdered;

    // تتبّع التنفيذ الجزئي:
    @Column(name = "qty_allocated", nullable = false)
    private Integer qtyAllocated = 0; // حجز مخزون
    @Column(name = "qty_shipped", nullable = false)
    private Integer qtyShipped = 0; // شُحن
    @Column(name = "qty_canceled", nullable = false)
    private Integer qtyCanceled = 0; // أُلغي

    @Column(name = "allow_backorder", nullable = false)
    private boolean allowBackorder;
    @Column(name = "preparation_days")
    private Float preparationDays;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItemSelection> selections = new ArrayList<>();

    public BigDecimal getUnitPrice() {
        return unitPriceBase.add(unitPriceExtra == null ? BigDecimal.ZERO : unitPriceExtra);
    }

    public BigDecimal getLineTotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(qtyOrdered));
    }

    public BigDecimal getTotalDeposit() {
        return (depositAmount != null ? depositAmount : BigDecimal.ZERO).multiply(BigDecimal.valueOf(qtyOrdered));
    }

    public BigDecimal getRemainingAmount() {
        return getLineTotal().subtract(getTotalDeposit());
    }

    @Transient
    public Integer getQtyBackordered() {
        return Math.max(0, qtyOrdered - qtyAllocated - qtyCanceled);
    }
}
