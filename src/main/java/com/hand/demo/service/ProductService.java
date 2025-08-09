package com.hand.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.hand.demo.model.Dtos.CreateProductRequest;
import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.AttributeValue;
import com.hand.demo.model.entity.AttributeValueImage;
import com.hand.demo.model.entity.Category;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.entity.ProductImage;
import com.hand.demo.model.entity.Tag;
import com.hand.demo.model.repository.CategoryRepository;
import com.hand.demo.model.repository.CompanyRepository;
import com.hand.demo.model.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final TagService tagService;

    private final ProductRepository productRepo;
    private final CompanyRepository companyRepo;
    private final CategoryRepository categoryRepo;
    private boolean main = false;

  

    public Product createProduct(CreateProductRequest request, Company company1) {
        Company company = company1;
        if (Objects.isNull(company1)) {
            company = companyRepo.findById(request.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        }
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setPreparationDays(request.getPreparationDays());
        product.setIsActive(request.getIsActive());
        product.setAvailabilityStatus(Product.AvailabilityStatus.IN_STOCK);
        product.setCompany(company);
        List<ProductImage> imageEntities = request.getImages().stream()
                .map(imgDto -> this.checkMainImage(imgDto, main, product))
                .toList();
        product.setImages(imageEntities);

        if (request.getCategoryIds() != null)

        {
            List<Category> categories = categoryRepo.findAllById(
                    request.getCategoryIds());
            product.setCategories(categories);
        }

            List<Tag> tags = tagService.getOrCreateTags(request.getTagNames());
            product.setTags(tags);

        List<Attribute> attributes = new ArrayList<>();
        if (request.getAttributes() != null) {
            for (CreateProductRequest.AttributeDTO attrDTO : request.getAttributes()) {
                Attribute attribute = new Attribute();
                attribute.setName(attrDTO.getName());
                attribute.setType(attrDTO.getType());
                attribute.setIsRequired(attrDTO.getIsRequired());
                attribute.setProduct(product);

                List<AttributeValue> values = new ArrayList<>();
                if (attrDTO.getValues() != null) {
                    for (CreateProductRequest.AttributeDTO.AttributeValueDTO valDTO : attrDTO.getValues()) {
                        AttributeValue val = new AttributeValue();
                        val.setValue(valDTO.getValue());
                        val.setAttribute(attribute);

                        if (valDTO.getImageUrls() != null) {
                            List<AttributeValueImage> imgs = valDTO.getImageUrls().stream()
                                    .map(url -> {
                                        AttributeValueImage img = new AttributeValueImage();
                                        img.setUrl(url);
                                        img.setAttributeValue(val);
                                        return img;
                                    }).toList();
                            val.setAttributeValueImages(imgs);
                        }

                        values.add(val);
                    }
                }

                attribute.setAttributeValues(values);
                attributes.add(attribute);
            }
        }

        product.setAttributes(attributes);

        return productRepo.save(product);
    }

    private ProductImage checkMainImage(CreateProductRequest.ProductImageDTO imgDto, Boolean main, Product product) {
        if (main == false) {
            this.main = imgDto.isMain();
        } else {
            imgDto.setMain(false);
        }
        return new ProductImage(imgDto.getUrl(), imgDto.isMain(), product);

    }
}