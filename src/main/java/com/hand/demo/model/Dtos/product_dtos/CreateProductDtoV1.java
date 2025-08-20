package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hand.demo.model.Dtos.CreateImageDto;
import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.AttributeValue;
import com.hand.demo.model.entity.Product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProductDtoV1 {

    @NotBlank(message = "name shouldn't be blank")
    private String name;

    @Size(max = 1000, message = "description shouldn't be longer than 500 characters")
    private String description;;

    @NotNull(message = "is active shouldn't be null")
    private Boolean isActive;

    private Long companyId;

    private List<Long> categoryIds;

    private List<CreateImageDto> images;

    private List<CreateAttributeDTO> attributes;
    private List<String> tagNames;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @Data
    public static class ProductImageDTO {
        private Long id;
        private boolean main;
    }

   /*  public Product DtoToProduct( Product product ) {
       
        product.setName(this.getName());
        product.setDescription(this.getDescription());
        product.setIsActive(this.getIsActive());
        List<Attribute> attributes1 =toAttributes(attributes, product);
        product.setAttributes(attributes1);
        return product;
    }*/

    // Constructor to convert from Product entity to DTO
    public List<Attribute> toAttributes(List<CreateAttributeDTO> request, Product product) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        if (request != null) {
            for (CreateAttributeDTO attrDTO : request) {
                Attribute attribute = new Attribute();
                attribute.setName(attrDTO.getName());
                if (attrDTO.getType() != null) {
                    switch (attrDTO.getType()) {
                        case TEXT:
                            attribute.setType(Attribute.AttributeType.TEXT);
                            break;
                        case NUMBER:
                            attribute.setType(Attribute.AttributeType.NUMBER);
                            break;
                        case SELECT:
                            attribute.setType(Attribute.AttributeType.SELECT);
                            break;
                        default:
                            attribute.setType(Attribute.AttributeType.TEXT);
                    }
                }

                attribute.setIsRequired(attrDTO.getIsRequired());
                attribute.setProduct(product);

                ArrayList<AttributeValue> values = new ArrayList<>();
                if (attrDTO.getValues() != null) {
                    for (CreateAttributeDTO.AttributeValueDTO valDTO : attrDTO.getValues()) {
                        AttributeValue val = new AttributeValue();
                        val.setValue(valDTO.getValue());
                        val.setAttribute(attribute);
                        val.setId(valDTO.getId());
                        values.add(val);
                    }
                }

                attribute.setAttributeValues(values);
                attributes.add(attribute);
            }
        }
        return attributes;
    }
}
