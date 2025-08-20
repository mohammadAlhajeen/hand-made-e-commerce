package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;

import com.hand.demo.model.entity.PreOrderProduct;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePreOrderProductDto extends CreateProductDtoV1 {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal prePaidPrice;

    @NotNull
    @Min(0)
    private Float preparationDays;

    public PreOrderProduct DtoToProduct() {
        // إنشاء منتج طلب مسبق جديد

        PreOrderProduct preOrderProduct = new PreOrderProduct();

        preOrderProduct.setName(this.getName());
        preOrderProduct.setDescription(this.getDescription());
        preOrderProduct.setIsActive(this.getIsActive());
        preOrderProduct.setPrice(this.getPrice());

        preOrderProduct.setPrePaidPrice(this.getPrePaidPrice());
        preOrderProduct.setPreparationDays(this.getPreparationDays());


        return preOrderProduct;
    }

    /*
     * // Constructor to convert from PreOrderProduct entity to DTO
     * public CreatePreOrderProductDto(PreOrderProduct product) {
     * super(product); // Call parent constructor
     * this.prePaidPrice = product.getPrePaidPrice();
     * this.preparationDays = product.getPreparationDays();
     * }
     */
    // Default constructor
    public CreatePreOrderProductDto() {
        super();
    }
}