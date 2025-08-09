package com.hand.demo.model.Dtos;

import lombok.Data;
import java.util.List;

@Data
public class CategoryProductDto {
    private Long productId;
    private List<Long> categoryIds;
}