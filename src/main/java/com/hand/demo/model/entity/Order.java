package com.hand.demo.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hand.demo.model.enums.OrderStatus;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
@Entity
@Table(name="orders", indexes=@Index(name="idx_order_company_status", columnList="company_id,status"))
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private Long version;

    @Column(name="order_number", unique=true, length=32, nullable=false)
    private String orderNumber;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="customer_id")
    private AppUser customer;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="company_id")
    private Company company;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private OrderStatus status = OrderStatus.CREATED;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    @JsonBackReference
    private List<OrderItem> items = new ArrayList<>();

    @Column(name="subtotal", precision=10, scale=2, nullable=false) private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name="shipping", precision=10, scale=2, nullable=false) private BigDecimal shipping = BigDecimal.ZERO;
    @Column(name="discount", precision=10, scale=2, nullable=false) private BigDecimal discount = BigDecimal.ZERO;
    @Column(name="total", precision=10, scale=2, nullable=false) private BigDecimal total = BigDecimal.ZERO;

    @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at") private LocalDateTime updatedAt;

    @PrePersist void p(){
        createdAt=LocalDateTime.now(); updatedAt=createdAt;
        if (orderNumber==null) orderNumber="ORD-"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();
    }

    @PrePersist void p(){
        createdAt=LocalDateTime.now(); updatedAt=createdAt;
        if (orderNumber==null) orderNumber="ORD-"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();
    }
    @PreUpdate void u(){updatedAt=LocalDateTime.now();}

    public void addItem(OrderItem item){item.setOrder(this); items.add(item);}
    public void recomputeTotals(){
        subtotal = items.stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        total = subtotal.add(shipping).subtract(discount);
    }
}
