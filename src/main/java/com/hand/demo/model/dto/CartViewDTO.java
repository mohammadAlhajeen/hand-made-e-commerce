package com.hand.demo.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartViewDTO(
        Long id,
        Long customerId,
        String customerName,
        Long companyId,
        String companyName,
        List<CartItemViewDTO> items,
        BigDecimal subtotal,
        BigDecimal totalDepositRequired, // إجمالي العربون المطلوب للسلة
        BigDecimal totalRemainingAmount, // إجمالي المبلغ المتبقي
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record CartItemViewDTO(
            Long id,
            Long productId,
            String productName,
            String productType, // "STOCK" or "PRE_ORDER"
            BigDecimal unitPriceBase,
            BigDecimal unitPriceExtra,
            BigDecimal unitPriceTotal, // unitPriceBase + unitPriceExtra
            BigDecimal depositAmount,  // مبلغ العربون للعنصر الواحد
            BigDecimal totalDeposit,   // إجمالي العربون للكمية
            Integer quantity,
            BigDecimal lineTotal, // unitPriceTotal * quantity
            BigDecimal remainingAmount, // المبلغ المتبقي بعد العربون
            Float preparationDays,
            List<SelectionViewDTO> selections
    ) {}

    public record SelectionViewDTO(
            Long attributeId,
            String attributeName,
            Long valueId,
            String valueText,
            BigDecimal extraPrice
    ) {}
}
