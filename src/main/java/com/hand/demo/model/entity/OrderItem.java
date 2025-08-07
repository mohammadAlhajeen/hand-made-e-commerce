package com.hand.demo.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
@SQLDelete(sql = "UPDATE order_items SET deleted = true WHERE id = ?")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "attribute_price", precision = 10, scale = 2)
    private BigDecimal attributePrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private List<OrderItemAttributeValue> attributeValues = new ArrayList<>(10);

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    private boolean deleted = false;

    // Calculated total price including attributes and tax
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        // Base price * quantity
        BigDecimal baseTotal = price.multiply(BigDecimal.valueOf(quantity));
        
        // Add attribute prices
        BigDecimal attrTotal = attributePrice != null ? attributePrice : BigDecimal.ZERO;
        
        // Add tax
        BigDecimal totalWithAttr = baseTotal.add(attrTotal);
        BigDecimal taxAmount = tax != null ? totalWithAttr.multiply(tax) : BigDecimal.ZERO;
        
        // Set total price
        this.totalPrice = totalWithAttr.add(taxAmount);
    }
}
