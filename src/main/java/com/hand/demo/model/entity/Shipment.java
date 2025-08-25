package com.hand.demo.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.hand.demo.model.enums.ShipmentStatus;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name="shipments")
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="order_id")
    private Order order;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    private LocalDateTime createdAt; private LocalDateTime updatedAt;
    @PrePersist void p(){createdAt=LocalDateTime.now(); updatedAt=createdAt;}
    @PreUpdate  void u(){updatedAt=LocalDateTime.now();}

    @OneToMany(mappedBy="shipment", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<ShipmentItem> items = new ArrayList<>();
}
