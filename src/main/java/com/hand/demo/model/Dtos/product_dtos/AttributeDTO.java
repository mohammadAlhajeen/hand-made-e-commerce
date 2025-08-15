package com.hand.demo.model.Dtos.product_dtos;
import java.util.List;

import com.hand.demo.model.entity.Attribute;

import lombok.Data;

@Data
    public  class AttributeDTO {
        private Long id;
        private String name;
        private Attribute.AttributeType type;
        private Boolean isRequired;
        private List<AttributeValueDTO> values;

        @Data
        public static class AttributeValueDTO {
            private Long id;
            private String value;
            private List<String> imageUrls;
        }
    }