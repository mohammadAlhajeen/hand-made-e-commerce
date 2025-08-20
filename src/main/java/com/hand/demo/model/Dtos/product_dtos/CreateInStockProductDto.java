package com.hand.demo.model.Dtos.product_dtos;

import com.hand.demo.model.entity.InStockProduct;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInStockProductDto extends CreateProductDtoV1 {
    @NotNull
    @Min(0)
    private Integer quantity;

    private boolean returnable = true;
    private Integer returnDays = 14;
    private boolean allowBackorder = false;

    public InStockProduct DtoToProduct() {
      
        InStockProduct stockProduct = new InStockProduct();
        stockProduct.setName(this.getName());
        stockProduct.setDescription(this.getDescription());
        stockProduct.setIsActive(this.getIsActive());
        stockProduct.setPrice(this.getPrice());

        stockProduct.setAllowBackorder(this.isAllowBackorder());
        stockProduct.setQuantity(this.getQuantity() != null && this.getQuantity() > 0 ? this.getQuantity() : 0);
        stockProduct.setReturnable(this.isReturnable());
        stockProduct.setReturnDays(this.getReturnDays());

        return stockProduct;
    }

   /* // Constructor to convert from InStockProduct entity to DTO
    public CreateStockProductDto(InStockProduct product) {
        super(product); // Call parent constructor
        this.quantity = product.getQuantity();
        this.returnable = product.isReturnable();
        this.returnDays = product.getReturnDays();
    }
*/
    // Default constructor
    public CreateInStockProductDto() {
        super();
    }

}
