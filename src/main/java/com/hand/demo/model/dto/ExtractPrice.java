package com.hand.demo.model.dto;

import java.math.BigDecimal;

import com.hand.demo.model.entity.PreOrderProduct;
import com.hand.demo.model.entity.Product;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExtractPrice {
    BigDecimal unitPriceBase;
    BigDecimal unitPriceTotal; // unitPriceBase + unitPriceExtra
    BigDecimal depositAmount = BigDecimal.ZERO; // مبلغ العربون للعنصر الواحد
    BigDecimal totalDeposit = BigDecimal.ZERO; // إجمالي العربون للكمية
    Integer quantity;
    BigDecimal lineTotal = BigDecimal.ZERO; // unitPriceTotal * quantity
    BigDecimal remainingAmount = BigDecimal.ZERO;

    public ExtractPrice(Product product, BigDecimal extraPrice, Integer quantity) {
        this.unitPriceBase = product.getPrice();
        this.unitPriceTotal = unitPriceBase.add(extraPrice);
        if (product instanceof PreOrderProduct preOrderProduct) {
            this.depositAmount = preOrderProduct.getPrePaidPrice();
            this.totalDeposit = depositAmount.multiply(BigDecimal.valueOf(quantity));
        }
        this.quantity = quantity;
        this.lineTotal = unitPriceTotal.multiply(BigDecimal.valueOf(quantity));
        this.remainingAmount = lineTotal.subtract(totalDeposit);
    }

}
