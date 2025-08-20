package com.hand.demo.model.Dtos.product_dtos;

import com.hand.demo.model.entity.InStockProduct;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetInStockProductDto extends GetProductDtoV1 {
    private Integer stockQuantity;
    private boolean returnable;
    private Integer returnDays;
    private boolean allowBackorder;

    public GetInStockProductDto(InStockProduct product) {
        super(product);
        this.stockQuantity = product.getQuantity(); // Use quantity field
        this.returnable = product.isReturnable();
        this.returnDays = product.getReturnDays();
        this.allowBackorder = product.isAllowBackorder();
    }

    // Default constructor
    public GetInStockProductDto() {
        super();
    }
}
