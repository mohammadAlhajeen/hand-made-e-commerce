package com.hand.demo.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull Long customerId,
        @NotNull Long productId,
        @Min(1) int quantity,
        List<Selection> selections // (attributeId, valueId)
) {
    public record Selection(@NotNull Long attributeId, @NotNull Long valueId) {}
}
