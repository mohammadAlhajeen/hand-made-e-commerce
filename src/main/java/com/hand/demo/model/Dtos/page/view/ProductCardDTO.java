package com.hand.demo.model.Dtos.page.view;
import java.math.BigDecimal;

public record ProductCardDTO(
        Long id,
        String name,
        BigDecimal price,
        String mainImageUrl)  {
}
