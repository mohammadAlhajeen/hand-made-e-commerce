package com.hand.demo.model.Dtos.product_dtos;

import com.hand.demo.model.entity.InStockProduct;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInStockProductDto extends CreateProductDtoV1 {

    private Integer totalQuantity = 0;

    private boolean returnable = true;
    private Integer returnDays = 14;
    private boolean allowBackorder = false;

    public InStockProduct DtoToProduct() {

        InStockProduct stockProduct = new InStockProduct();
        
        stockProduct.setName(this.getName());
        stockProduct.setTotalQuantity(this.getTotalQuantity());
        stockProduct.setQuantityCommitted(0); // Default to 0, can be updated later
        stockProduct.setDescription(this.getDescription());
        stockProduct.setIsActive(this.getIsActive());
        stockProduct.setPrice(this.getPrice());
        stockProduct.setReturnable(this.isReturnable());
        stockProduct.setReturnDays(this.getReturnDays());

        return stockProduct;
    }

    /*
     * // Constructor to convert from InStockProduct entity to DTO
     * public CreateStockProductDto(InStockProduct product) {
     * super(product); // Call parent constructor
     * this.quantity = product.getQuantity();
     * this.returnable = product.isReturnable();
     * this.returnDays = product.getReturnDays();
     * }
     */
    // Default constructor
    public CreateInStockProductDto() {
        super();
    }

}
