package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;

import com.hand.demo.model.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GetProductCardDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl; // Assuming you want to show a single image URL for the card
    private Integer preparationDays;
    private Boolean isActive;
    
    public GetProductCardDto(Product product) {
        if (this.isActive() == false)
            return null;
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice().compareTo(BigDecimal.ZERO) > 0 ? product.getPrice() : null;
        this.preparationDays = product.getPreparationDays();
        if (!product.getImages().isEmpty()) {
            this.imageUrl = product.getImages().get(0).getUrl(); // Assuming the first image is the main one
        }
    }
}
