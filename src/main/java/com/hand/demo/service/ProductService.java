package com.hand.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hand.demo.model.Dtos.GetProductDto;
import com.hand.demo.model.Dtos.RatingDistributionDto;
import com.hand.demo.model.Dtos.product_dtos.CreateProductDto;
import com.hand.demo.model.entity.Category;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.entity.ProductImage;
import com.hand.demo.model.entity.Tag;
import com.hand.demo.model.repository.CategoryRepository;
import com.hand.demo.model.repository.GetProductCardProjection;
import com.hand.demo.model.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final TagService tagService;

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private boolean main = false;

    // ##############################
    // #########Create Product#######
    // ##############################
    public Product createProduct(CreateProductDto request, Company company) {
        Product product = request.DtoToProduct(request);
        product.setCompany(company);

        List<ProductImage> imageEntities = request.getImages().stream()
                .map(imgDto -> this.checkMainImage(imgDto, main, product))
                .toList();
        product.setImages(imageEntities);

        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepo.findAllById(
                    request.getCategoryIds());
            product.setCategories(categories);
        }

        List<Tag> tags = tagService.getOrCreateTags(request.getTagNames());
        product.setTags(tags);
        product.setCompany(company);
        return productRepo.save(product);
    }

    private ProductImage checkMainImage(CreateProductDto.ProductImageDTO imgDto, Boolean main, Product product) {
        if (main == false) {
            this.main = imgDto.isMain();
        } else {
            imgDto.setMain(false);
        }
        return new ProductImage(imgDto.getUrl(), imgDto.isMain(), product);

    }

    // ##############################
    // ######### Get Product ########
    // ##############################
    public GetProductDto getProductDtoById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
        return new GetProductDto(product);
    }

    public List<com.hand.demo.model.repository.GetProductCardProjection> getProductCardLists(Company company) {

        List<com.hand.demo.model.repository.GetProductCardProjection> productCardDto = productRepo
                .findAllProjectedByCompanyId(company.getId());
        return productCardDto;

    }

    // Search products cards by product name (native search function)
    public List<GetProductCardProjection> searchProductCards(String productName) {
        if (productName == null) {
            productName = "";
        }
        return productRepo.searchGetProductCardProjections(productName);
    }

    // Get a single product card projection by exact product name
    public GetProductCardProjection getProductCardByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return productRepo.findProjectedByName(name);
    }

    // ##############################
    // #### Rating distribution #####
    // ##############################
    public RatingDistributionDto getRatingDistribution(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        var mr = product.getAvgRating();
        if (mr == null) {
            return new RatingDistributionDto(0, 0, 0, 0, 0, 0, BigDecimal.ZERO);
        }
        long count = mr.getRatingCount();
        long one = count == 0 ? 0 : (mr.getOneRating() * 100) / count;
        long two = count == 0 ? 0 : (mr.getTwoRating() * 100) / count;
        long three = count == 0 ? 0 : (mr.getThreeRating() * 100) / count;
        long four = count == 0 ? 0 : (mr.getFourRating() * 100) / count;
        long five = count == 0 ? 0 : (mr.getFiveRating() * 100) / count;
        BigDecimal avg = (mr.getAverageRating() == null ? BigDecimal.ZERO : mr.getAverageRating());
        return new RatingDistributionDto(one, two, three, four, five, count, avg);
    }

}