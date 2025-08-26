package com.hand.demo.model.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id")
    @JsonManagedReference
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderItemType type;




    @Min(1)
    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "preparation_days")
    private Float preparationDays;

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CartItemSelection> selections = new ArrayList<>();


    public String selectionKey() {
        return selections.stream()
                .sorted(Comparator.comparing(CartItemSelection::getAttributeId)
                        .thenComparing(CartItemSelection::getValueId))
                .map(s -> s.getAttributeId() + ":" + s.getValueId())
                .collect(Collectors.joining("|"));
    }

    public boolean sameSelections(CartItem other) {
        return Objects.equals(this.selectionKey(), other.selectionKey());
    }
}
