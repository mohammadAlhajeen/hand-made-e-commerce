package com.hand.demo.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShipmentRequest(
        @NotBlank String orderNumber,
        @NotNull List<Line> items // orderItemId + qty
) {
    public record Line(@NotNull Long orderItemId, @Min(1) int qty) {}
}
