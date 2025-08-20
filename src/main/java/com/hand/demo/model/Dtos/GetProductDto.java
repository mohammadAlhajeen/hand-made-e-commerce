package com.hand.demo.model.Dtos;

import java.util.List;

import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.ProductImage;

import lombok.Data;

@Data
public class GetProductDto {
    private Long id;
    private Long company_id;
    private String company_name;
    private String name;
    private String description;
    private Double price;
    private List<ProductImage> productImages;
    private List<Attribute> attributes;

    public GetProductDto(com.hand.demo.model.entity.Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice() != null ? product.getPrice().doubleValue() : null;
        if (product.getCompany() != null) {
            this.company_id = product.getCompany().getId();
            this.company_name = product.getCompany().getName();
        }
        this.productImages = product.getImages();
        this.attributes = product.getAttributes();
    }
}