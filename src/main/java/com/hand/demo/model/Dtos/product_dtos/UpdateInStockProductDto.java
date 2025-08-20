package com.hand.demo.model.Dtos.product_dtos;

import com.hand.demo.model.entity.InStockProduct;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateInStockProductDto extends UpdateProductDtoV1 {

    @Min(value = 0, message = "Stock quantity must be >= 0")
    private Integer stockQuantity;

    private Boolean returnable;

    @Min(value = 0, message = "Return days must be >= 0")
    private Integer returnDays;

    private Boolean allowBackorder;

    public InStockProduct updateProduct(InStockProduct existingProduct) {
        // Update common fields from parent
        if (this.getName() != null) {
            existingProduct.setName(this.getName());
        }
        if (this.getDescription() != null) {
            existingProduct.setDescription(this.getDescription());
        }
        if (this.getPrice() != null) {
            existingProduct.setPrice(this.getPrice());
        }
        if (this.getQuantity() != null) {
            existingProduct.setQuantity(this.getQuantity());
        }
        if (this.getIsActive() != null) {
            existingProduct.setIsActive(this.getIsActive());
        }


        // Update InStock specific fields
        if (this.getStockQuantity() != null) {
            existingProduct.setQuantity(this.getStockQuantity()); // Use quantity field
        }
        if (this.getReturnable() != null) {
            existingProduct.setReturnable(this.getReturnable());
        }
        if (this.getReturnDays() != null) {
            existingProduct.setReturnDays(this.getReturnDays());
        }
        if (this.getAllowBackorder() != null) {
            existingProduct.setAllowBackorder(this.getAllowBackorder());
        }

        return existingProduct;
    }
}
