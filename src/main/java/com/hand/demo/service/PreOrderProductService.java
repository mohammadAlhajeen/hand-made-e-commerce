package com.hand.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.Dtos.product_dtos.CreateAttributeDTO;
import com.hand.demo.model.Dtos.product_dtos.CreatePreOrderProductDto;
import com.hand.demo.model.Dtos.product_dtos.PreOrderProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.UpdatePreOrderProductDto;
import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.AttributeValue;
import com.hand.demo.model.entity.AttributeValueImage;
import com.hand.demo.model.entity.Category;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.PreOrderProduct;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.entity.Tag;
import com.hand.demo.model.repository.AttributeRepository;
import com.hand.demo.model.repository.CategoryRepository;
import com.hand.demo.model.repository.PreOrderProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PreOrderProductService {

    private final TagService tagService;

    private final PreOrderProductRepository preOrderProductRepo;
    private final CategoryRepository categoryRepo;
    private final ProductImageAssignService mediaService;
    private final AttributeRepository attributeRepository;

    // ##############################
    // ######## Create Product ######
    // ##############################
    @Transactional
    public PreOrderProduct createPreOrderProduct(CreatePreOrderProductDto request, Company company) {
        PreOrderProduct product = request.DtoToProduct();

        // إعداد العلاقات قبل الحفظ
        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepo.findAllById(
                    request.getCategoryIds());
            product.setCategories(categories);
        }

        List<Tag> tags = tagService.getOrCreateTags(request.getTagNames());
        product.setTags(tags);
        product.setCompany(company);

        // حفظ المنتج أولاً للحصول على ID
        PreOrderProduct savedProduct = preOrderProductRepo.save(product);

        // الآن يمكن إضافة الصور بأمان
        if (request.getImages() != null) {
            mediaService.assignImagesToProduct(savedProduct, request.getImages());
            // حفظ مرة أخرى لتحديث الصور
        }
        if (request.getAttributes() != null) {
            toAttributes(request.getAttributes(), savedProduct);
        }
        savedProduct = preOrderProductRepo.save(savedProduct);

        return savedProduct;
    }

    public PreOrderProductForCompanyV1 createOrderProduct(CreatePreOrderProductDto request, Company company) {
        PreOrderProduct product = createPreOrderProduct(request, company);
        return PreOrderProductForCompanyV1.fromProduct(product);
    }
    // ##############################
    // ######### Update Product ########
    // ##############################

    @Transactional
    public PreOrderProductForCompanyV1 updateProduct(UpdatePreOrderProductDto dto, Long productId, Long companyId) {
        if (dto == null) {
            throw new IllegalArgumentException("update dto must not be null");
        }

        PreOrderProduct p = preOrderProductRepo.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        dto.updateProduct(p);

        if (dto.getCategoryIds() != null) {
            List<Long> ids = dto.getCategoryIds();
            p.getCategories().clear();
            List<Category> cats = categoryRepo.findAllById(ids);
            p.setCategories(new ArrayList<>(cats));
        }

        if (dto.getTagNames() != null) {
            if (dto.getTagNames().isEmpty()) {
                p.getTags().clear();
            } else {
                List<Tag> tags = tagService.getOrCreateTags(dto.getTagNames());
                p.setTags(tags);
            }
        }

        if (dto.getImages() != null) {
            if (p.getImages() != null) {
                p.getImages().clear();
            }
            if (!dto.getImages().isEmpty()) {
                mediaService.assignImagesToProduct(p, dto.getImages());
            }
        }
        if (dto.getAttributes() != null) {
            p.getAttributes().clear();
            if (!dto.getAttributes().isEmpty()) {
                toAttributes(dto.getAttributes(), p);
            }
        }

        PreOrderProduct saved = preOrderProductRepo.save(p);
        return PreOrderProductForCompanyV1.fromProduct(saved);
    }

    public List<Attribute> toAttributes(List<CreateAttributeDTO> request, Product product) {
        if (request == null) {
            return new ArrayList<>();
        }
        List<Attribute> att = new ArrayList<>();
        for (CreateAttributeDTO dto : request) {
            att.add(mapToAttribute(dto, product));
        }
        product.setAttributes(att);
        return att;
    }

    private Attribute mapToAttribute(CreateAttributeDTO dto, Product product) {
        Attribute attribute = new Attribute();
        attribute.setName(dto.getName());
        attribute.setType(
                dto.getType() != null ? dto.getType() : Attribute.AttributeType.TEXT);
        attribute.setIsRequired(dto.getIsRequired());
        attribute.setProduct(product);
        List<AttributeValue> values = new ArrayList<>();
        if (dto.getValues() != null) {
            for (CreateAttributeDTO.AttributeValueDTO valDTO : dto.getValues()) {
                values.add(mapToAttributeValue(valDTO, attribute));
            }
        }

        attribute.setAttributeValues(values);
        return attribute;
    }

    private AttributeValue mapToAttributeValue(CreateAttributeDTO.AttributeValueDTO valDTO, Attribute attribute) {
        AttributeValue val = new AttributeValue();
        val.setValue(valDTO.getValue());
        val.setAttribute(attribute);

        List<AttributeValueImage> images = new ArrayList<>();
        var mediaItems = mediaService.getMediaItemsByIds(valDTO.getImage());
        if (mediaItems != null) {
            for (var mediaItem : mediaItems) {
                AttributeValueImage attValImg = new AttributeValueImage();
                attValImg.setMediaItem(mediaItem);
                attValImg.setAttributeValue(val);
                images.add(attValImg);
            }
        }

        val.setAttributeValueImages(images);
        System.out.println(val.getAttributeValueImages().toString());
        return val;
    }

}
