package com.hand.demo.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.hand.demo.model.enums.OrderStatus;

public record OrderViewDTO(
        Long id,
        String orderNumber,
        Long customerId,
        String customerName,
        Long companyId,
        String companyName,
        OrderStatus status,
        List<OrderItemViewDTO> items,
        BigDecimal subtotal,
        BigDecimal depositRequired,
        BigDecimal depositPaid,
        BigDecimal remainingAmount,
        BigDecimal shipping,
        BigDecimal discount,
        BigDecimal total,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record OrderItemViewDTO(
            Long id,
            Long productId,
            String productName,
            String productType,
            BigDecimal unitPriceBase,
            BigDecimal unitPriceExtra,
            BigDecimal unitPriceTotal,
            BigDecimal depositAmount,
            BigDecimal totalDeposit,
            Integer qtyOrdered,
            Integer qtyAllocated,
            Integer qtyShipped,
            Integer qtyCanceled,
            Integer qtyBackordered,
            BigDecimal lineTotal,
            BigDecimal remainingAmount,
            boolean allowBackorder,
            Float preparationDays,
            List<OrderItemSelectionViewDTO> selections
    ) {}

    public record OrderItemSelectionViewDTO(
            Long attributeId,
            String attributeName,
            Long valueId,
            String valueText,
            BigDecimal extraPrice
    ) {}
}
