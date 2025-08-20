package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hand.demo.model.Dtos.CreateImageDto;
import com.hand.demo.model.entity.Product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProductDtoV1 {

    private String name;

    @Size(max = 1000, message = "description shouldn't be longer than 500 characters")
    private String description;

    @DecimalMin(value = "0", message = "price must be positive")
    private BigDecimal price;
    @DecimalMin(value = "0", message = "quantity must be positive")
    private Integer quantity;

    private Boolean isActive;

    private Long companyId;

    private List<Long> categoryIds;

    private List<CreateImageDto> images;

    private ArrayList<CreateAttributeDTO> attributes = new ArrayList<>();
    private List<String> tagNames;

    @Data
    public static class ProductImageDTO {

        private String url;
        private boolean main;
    }

    public Product DtoToProduct(Product product) {

        product.setName(this.getName());
        product.setDescription(this.getDescription());
        product.setPrice(this.getPrice());

        return product;
    }

    
    
}
