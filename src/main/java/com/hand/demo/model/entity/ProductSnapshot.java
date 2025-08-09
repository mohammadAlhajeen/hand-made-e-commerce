package com.hand.demo.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Embeddable
@Data
public class ProductSnapshot {

    @Column(name = "product_id")
    private Long originalProductId;

    private String name;
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private String thumbnailUrl;

    private String companyName;

    @ElementCollection
    @CollectionTable(name = "order_item_categories", joinColumns = @JoinColumn(name = "order_item_id"))
    @Column(name = "category_name")
    private List<String> categories = new ArrayList<>();
}
