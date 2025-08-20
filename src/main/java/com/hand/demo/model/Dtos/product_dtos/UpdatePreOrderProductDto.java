package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;

import com.hand.demo.model.entity.PreOrderProduct;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdatePreOrderProductDto extends UpdateProductDtoV1 {
    
    @DecimalMin(value = "0.0", message = "Prepaid price must be >= 0")
    private BigDecimal prePaidPrice;

    @Min(value = 0, message = "Preparation days must be >= 0")
    private Float preparationDays;

    public PreOrderProduct updateProduct(PreOrderProduct existingProduct) {
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

        if (this.getIsActive() != null) {
            existingProduct.setIsActive(this.getIsActive());
        }
        
        // Update PreOrder specific fields
        if (this.getPrePaidPrice() != null) {
            existingProduct.setPrePaidPrice(this.getPrePaidPrice());
        }
        if (this.getPreparationDays() != null) {
            existingProduct.setPreparationDays(this.getPreparationDays());
        }

        
        return existingProduct;
    }
}
