package com.hand.demo.model.Dtos;

import java.math.BigDecimal;
import java.util.List;

import com.hand.demo.model.entity.Attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank(message = "name shouldn't be blank")
    private String name;

    private String description;

    @NotNull(message = "price shouldn't be null")
    private BigDecimal price;

    @NotNull(message = " quantity shouldn't be null")
    private Integer quantity;

    private Integer preparationDays;

    @NotNull(message = "is active shouldn't be null")
    private Boolean isActive;

    private Long companyId;

    private List<Long> categoryIds;

    private List<ProductImageDTO> images;

    private List<AttributeDTO> attributes;
    private List<String> tags;

    @Data
    public static class tagsDTO {
        private String tagName;
    }
    @Data
    public static class ProductImageDTO {
        private String url;
        private boolean main;
    }

    @Data
    public static class AttributeDTO {
        private String name;
        private Attribute.AttributeType type;
        private Boolean isRequired;
        private List<AttributeValueDTO> values;

        @Data
        public static class AttributeValueDTO {
            private String value;
            private List<String> imageUrls; // ممكن أكثر من صورة
        }
    }
}
