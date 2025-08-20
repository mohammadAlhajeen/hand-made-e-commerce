package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;

import com.hand.demo.model.entity.PreOrderProduct;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetPreOrderProductDto extends GetProductDtoV1 {
    private BigDecimal prePaidPrice;
    private Float preparationDays;

    public GetPreOrderProductDto(PreOrderProduct product) {
        super(product);
        this.prePaidPrice = product.getPrePaidPrice();
        this.preparationDays = product.getPreparationDays();
    }

    // Default constructor
    public GetPreOrderProductDto() {
        super();
    }
}
