package com.hand.demo.model.Dtos.product_dtos;

import com.hand.demo.model.Dtos.CreateImageDto;
import com.hand.demo.model.Dtos.GetImageDtoProduct;
import java.math.BigDecimal;
import java.util.List;

import com.hand.demo.model.Dtos.GetImages;
import com.hand.demo.model.Dtos.product_dtos.AttributeDTO.AttributeValueDTO;
import com.hand.demo.model.entity.InStockProduct;
import com.hand.demo.model.entity.ProductImage;
import java.util.stream.Collector;

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
public final class InStockProductForCompanyV1 implements ProductForCompanyV1 {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isActive;
    private Long companyId;
    private List<Long> categoryIds;
    private List<GetImageDtoProduct> images;
    private List<AttributeDTO> attributes;
    private List<String> tagNames;
    private Integer quantity;
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
        dto.setCompanyId(product.getCompany() != null ? product.getCompany().getId() : null);
        // التصنيفات
        if (product.getCategories() != null) {
            dto.setCategoryIds(product.getCategories().stream().map(c -> c.getId()).toList());
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

        dto.setQuantity(product.getQuantity());
        dto.setReturnable(product.isReturnable());
        dto.setReturnDays(product.getReturnDays());
        dto.setAllowBackorder(product.isAllowBackorder());
        return dto;
    }
}
