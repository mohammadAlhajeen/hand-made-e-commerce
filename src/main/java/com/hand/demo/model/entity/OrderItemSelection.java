package com.hand.demo.model.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "order_item_selections")
public class OrderItemSelection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="order_item_id")
    @JsonBackReference
    private OrderItem orderItem;

    @Column(name="attribute_id", nullable=false) private Long attributeId;
    @Column(name="attribute_name", nullable=false, length=120) private String attributeName;
    @Column(name="value_id", nullable=false) private Long valueId;
    @Column(name="value_text", nullable=false, length=255) private String valueText;
    @Column(name="extra_price", precision=10, scale=2) private BigDecimal extraPrice = BigDecimal.ZERO;
}
