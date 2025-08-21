package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;
import java.util.List;

import com.hand.demo.model.Dtos.image_dtos.GetImageDtoProduct;
import com.hand.demo.model.Dtos.image_dtos.GetImages;
import com.hand.demo.model.Dtos.product_dtos.AttributeDTO.AttributeValueDTO;
import com.hand.demo.model.entity.Category;
import com.hand.demo.model.entity.InStockProduct;

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
public final class InStockProductForCompanyV1 implements ProductDTOs {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isActive;
    private List<Category> categories;
    private List<GetImageDtoProduct> images;
    private List<AttributeDTO> attributes;
    private List<String> tagNames;
    private Integer totalQuantity = 0;
    private Integer quantityAvailable = 0;
    private Integer quantityCommitted = 0;
    private boolean returnable;
    private Integer returnDays;
    private boolean allowBackorder;

    // دالة تحويل من Product إلى ProductForCompany
    public static InStockProductForCompanyV1 fromProduct(InStockProduct product) {
        InStockProductForCompanyV1 dto = new InStockProductForCompanyV1();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setIsActive(product.getIsActive());
        dto.setReturnable(product.isReturnable());
        dto.setReturnDays(product.getReturnDays());
        dto.setAllowBackorder(product.isAllowBackorder());
        // التصنيفات
        if (product.getCategories() != null) {
            dto.setCategories(product.getCategories());
        }
        // الصور
        if (product.getImages() != null) {
            List<GetImageDtoProduct> img = product.getImages()
                    .stream()
                    .map(img1 -> new GetImageDtoProduct(img1.getId(), img1.isMain(),img1.getMedia().getAbsoluteUrl(),img1.getSortOrder()))
                    .toList() ;
                    
            dto.setImages(img);
        }
        // التاجات

        if (product.getTags()
                != null) {
            dto.setTagNames(product.getTags().stream().map(t -> t.getName()).toList());
        }
        // الخصائص

        if (product.getAttributes()
                != null) {
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
                            List<GetImages> images = val.getAttributeValueImages().stream()
                                    .map(img -> new GetImages(img.getId(), img.getUrl()))
                                    .toList();
                            valDto.setImage(images);
                        }
                        return valDto;
                    }).toList());
                }
                return attrDto;
            }).toList());
        }

        dto.setQuantityAvailable(product.getQuantityAvailable());
        dto.setQuantityCommitted(product.getQuantityCommitted());
        dto.setTotalQuantity(product.getTotalQuantity());
        dto.setReturnable(product.isReturnable());
        dto.setReturnDays(product.getReturnDays());
        dto.setAllowBackorder(product.isAllowBackorder());
        return dto;
    }
}
