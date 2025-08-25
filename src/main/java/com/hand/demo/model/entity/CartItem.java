package com.hand.demo.model.entity;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hand.demo.model.enums.OrderItemType;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="cart_id")
    @JsonManagedReference
    private Cart cart;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="product_id")
    private Product product;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10)
    private OrderItemType type;

    @Column(name="product_name", nullable=false)
    private String productNameSnapshot;

    @Column(name="unit_price_base", nullable=false, precision=10, scale=2)
    private BigDecimal unitPriceBase;

    @Column(name="unit_price_extra", nullable=false, precision=10, scale=2)
    private BigDecimal unitPriceExtra = BigDecimal.ZERO;

    @Min(1) @Column(nullable=false) private Integer quantity = 1;

    @Column(name="preparation_days") private Float preparationDays;

    @OneToMany(mappedBy="cartItem", cascade=CascadeType.ALL, orphanRemoval=true)
    @JsonManagedReference
    private List<CartItemSelection> selections = new ArrayList<>();

    public BigDecimal getUnitPrice(){return unitPriceBase.add(unitPriceExtra==null?BigDecimal.ZERO:unitPriceExtra);}
    public BigDecimal getLineTotal(){return getUnitPrice().multiply(BigDecimal.valueOf(quantity));}

    /** مفتاح منطقي لدمج الأسطر إذا كانت نفس الاختيارات */
    public String selectionKey(){
        return selections.stream()
            .sorted(Comparator.comparing(CartItemSelection::getAttributeId)
                              .thenComparing(CartItemSelection::getValueId))
            .map(s -> s.getAttributeId()+":"+s.getValueId())
            .collect(Collectors.joining("|"));
    }
    public boolean sameSelections(CartItem other){return Objects.equals(this.selectionKey(), other.selectionKey());}
}
