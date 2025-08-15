package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;
import java.util.List;

import com.hand.demo.model.Dtos.product_dtos.AttributeDTO.AttributeValueDTO;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.entity.ProductImage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

@Data
public class ProductForCompany {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Integer preparationDays;
    private Boolean isActive;
    private Long companyId;
    private List<Long> categoryIds;
    private Product.AvailabilityStatus availabilityStatus;
    private List<ProductImage> images;
    private List<AttributeDTO> attributes;
    private List<String> tagNames;




    // دالة تحويل من Product إلى ProductForCompany
    public static ProductForCompany fromProduct(Product product) {
        ProductForCompany dto = new ProductForCompany();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setPreparationDays(product.getPreparationDays());
        dto.setIsActive(product.getIsActive());
        dto.setCompanyId(product.getCompany() != null ? product.getCompany().getId() : null);
        dto.setAvailabilityStatus(product.getAvailabilityStatus());
        // التصنيفات
        if (product.getCategories() != null) {
            dto.setCategoryIds(product.getCategories().stream().map(c -> c.getId()).toList());
        }
        // الصور
        if (product.getImages() != null) {
            dto.setImages(product.getImages());
        }
        // التاجات
        if (product.getTags() != null) {
            dto.setTagNames(product.getTags().stream().map(t -> t.getName()).toList());
        }
        // الخصائص
        if (product.getAttributes() != null) {
            dto.setAttributes(product.getAttributes().stream().map(attr -> {
                AttributeDTO attrDto = new AttributeDTO();
                attrDto.setId(attr.getId());
                attrDto.setName(attr.getName());
                attrDto.setType(attr.getType());
                attrDto.setIsRequired(attr.getIsRequired());
                if (attr.getAttributeValues() != null) {
                    attrDto.setValues(attr.getAttributeValues().stream().map(val -> {
                        AttributeValueDTO valDto = new AttributeValueDTO();
                        valDto.setId(val.getId());
                        valDto.setValue(val.getValue());
                        if (val.getAttributeValueImages() != null) {
                            valDto.setImageUrls(val.getAttributeValueImages().stream().map(img -> img.getUrl()).toList());
                        }
                        return valDto;
                    }).toList());
                }
                return attrDto;
            }).toList());
        }
        return dto;
    }
}

