package com.hand.demo.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull Long cartId,
        @NotNull Long cartItemId,
        @Min(1) int quantity,
        List<Selection> selections
) {
    public record Selection(@NotNull Long attributeId, @NotNull Long valueId) {}
}
