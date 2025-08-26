package com.hand.demo.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PayDepositRequest(
        @NotNull String orderNumber,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {}
