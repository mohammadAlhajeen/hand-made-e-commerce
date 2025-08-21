package com.hand.demo.model.Dtos.product_dtos;

import java.math.BigDecimal;
import java.util.List;

import com.hand.demo.model.Dtos.image_dtos.GetImageDtoProduct;
import com.hand.demo.model.Dtos.image_dtos.GetImages;
import com.hand.demo.model.Dtos.product_dtos.AttributeDTO.AttributeValueDTO;
import com.hand.demo.model.entity.Category;
import com.hand.demo.model.entity.PreOrderProduct;

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
public final class PreOrderProductForCompanyV1 implements ProductDTOs {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isActive;
    private Long companyId;
    private List<Category> categories;
    private List<GetImageDtoProduct> images;
    private List<AttributeDTO> attributes;
    private List<String> tagNames;

    private BigDecimal prePaidPrice;
    private Float preparationDays;

    // دالة تحويل من Product إلى ProductForCompany
    public static PreOrderProductForCompanyV1 fromProduct(PreOrderProduct product) {
        PreOrderProductForCompanyV1 dto = new PreOrderProductForCompanyV1();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setIsActive(product.getIsActive());
        dto.setCompanyId(product.getCompany() != null ? product.getCompany().getId() : null);
        if (product.getCategories() != null) {
            dto.setCategories(product.getCategories());
        }

        if (product.getImages() != null) {
            List<GetImageDtoProduct> img = product.getImages()
                    .stream()
                    .map(img1 -> new GetImageDtoProduct(img1.getId(), img1.isMain(), img1.getMedia().getAbsoluteUrl(), img1.getSortOrder()))
                    .toList();

            dto.setImages(img);
        }

        if (product.getTags() != null) {
            dto.setTagNames(product.getTags().stream().map(t -> t.getName()).toList());
        }
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
        dto.setPrePaidPrice(product.getPrePaidPrice());
        dto.setPreparationDays(product.getPreparationDays());
        return dto;
    }
}
