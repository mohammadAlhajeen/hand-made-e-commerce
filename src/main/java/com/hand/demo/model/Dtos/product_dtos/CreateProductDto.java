package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.AttributeValue;
import com.hand.demo.model.entity.AttributeValueImage;
import com.hand.demo.model.entity.Product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductDto {

    @NotBlank(message = "name shouldn't be blank")
    private String name;

    private String description;

    @NotNull(message = "price shouldn't be null")
    private BigDecimal price;

    private Integer quantity;

    private Integer preparationDays;

    @NotNull(message = "is active shouldn't be null")
    private Boolean isActive;

    private Long companyId;

    private List<Long> categoryIds;
    private Product.AvailabilityStatus availabilityStatus;
    private List<ProductImageDTO> images;

    private List<AttributeDTO> attributes;
    private List<String> tagNames;

    @Data
    public static class ProductImageDTO {
        private String url;
        private boolean main;
    }
    public Product DtoToProduct(CreateProductDto request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setPreparationDays(request.getPreparationDays());
        product.setIsActive(request.getIsActive());
        product.setAvailabilityStatus(request.getAvailabilityStatus());

        List<Attribute> attributes = new ArrayList<>();
        if (request.getAttributes() != null) {
            for (AttributeDTO attrDTO : request.getAttributes()) {
                Attribute attribute = new Attribute();
                attribute.setName(attrDTO.getName());
                attribute.setType(attrDTO.getType());
                attribute.setIsRequired(attrDTO.getIsRequired());
                attribute.setProduct(product);

                List<AttributeValue> values = new ArrayList<>();
                if (attrDTO.getValues() != null) {
                    for (AttributeDTO.AttributeValueDTO valDTO : attrDTO.getValues()) {
                        AttributeValue val = new AttributeValue();
                        val.setValue(valDTO.getValue());
                        val.setAttribute(attribute);
                        if (valDTO.getImageUrls() != null) {
                            List<AttributeValueImage> imgs = valDTO.getImageUrls().stream()
                                    .map(url -> {
                                        AttributeValueImage img = new AttributeValueImage();
                                        img.setUrl(url);
                                        img.setAttributeValue(val);
                                        return img;
                                    }).toList();
                            val.setAttributeValueImages(imgs);
                        }

                        values.add(val);
                    }
                }

                attribute.setAttributeValues(values);
                attributes.add(attribute);
            }
        }

        product.setAttributes(attributes);
        return product;
    }
}
