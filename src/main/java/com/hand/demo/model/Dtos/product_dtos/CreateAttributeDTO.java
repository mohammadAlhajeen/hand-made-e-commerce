/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hand.demo.model.Dtos.product_dtos;

import java.util.List;
import java.util.UUID;

import com.hand.demo.model.entity.Attribute;

import lombok.Data;

/**
 *
 * @author Mohammad
 */
@Data
public class CreateAttributeDTO {
    private Long id;
    private String name;
    private Attribute.AttributeType type; // TEXT, NUMBER, SELECT
    private Boolean isRequired;
    private List<AttributeValueDTO> values;

    @Data
    public static class AttributeValueDTO {
        private Long id;
        private String value;
        private List<UUID> image;

    }
   
}

