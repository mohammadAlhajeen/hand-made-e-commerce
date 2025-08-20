package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;
import java.util.List;

import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.ProductImage;

import lombok.Data;

@Data
public class GetProductDtoV1 {
    private Long id;
    private Long company_id;
    private String company_name;
    private String name;
    private String description;
    private BigDecimal price;
    private List<ProductImage> productImages;
    private List<Attribute> attributes;
    private BigDecimal rating;

    public GetProductDtoV1(com.hand.demo.model.entity.Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice() != null ? product.getPrice() : null;
        if (product.getCompany() != null) {
            this.company_id = product.getCompany().getId();
            this.company_name = product.getCompany().getName();
        }
        this.productImages = product.getImages();
        this.attributes = product.getAttributes();
        this.rating = product.getAvgRating().getAverageRating() != null ? product.getAvgRating().getAverageRating()
                : null;
    }

    // Default constructor
    public GetProductDtoV1() {
    }
}