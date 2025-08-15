package com.hand.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.Dtos.GetProductDto;
import com.hand.demo.model.Dtos.RatingDistributionDto;
import com.hand.demo.model.Dtos.product_dtos.CreateProductDto;
import com.hand.demo.model.Dtos.product_dtos.ProductForCompany;
import com.hand.demo.model.entity.Category;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.entity.ProductImage;
import com.hand.demo.model.entity.Tag;
import com.hand.demo.model.repository.CategoryRepository;
import com.hand.demo.model.repository.CompanyProductProjection;
import com.hand.demo.model.repository.GetProductCardProjection;
import com.hand.demo.model.repository.GetReviewsProjection;
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
    // ######## Create Product ######
    // ##############################
    @Transactional
    public ProductForCompany createProduct(CreateProductDto request, Company company) {
        Product product = request.DtoToProduct(request);
        product.setCompany(company);

        checkMainImage(request, product);

        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepo.findAllById(
                    request.getCategoryIds());
            product.setCategories(categories);
        }

        List<Tag> tags = tagService.getOrCreateTags(request.getTagNames());
        product.setTags(tags);
        product.setCompany(company);
        return ProductForCompany.fromProduct(productRepo.save(product));
    }

    private void checkMainImage(CreateProductDto dto, Product product) {
        if (dto.getImages() != null) {
            List<ProductImage> newImages = new java.util.ArrayList<>();
            boolean mainPicked = false;
            for (CreateProductDto.ProductImageDTO imgDto : dto.getImages()) {
                boolean isMain = imgDto.isMain() && !mainPicked;
                if (isMain) {
                    mainPicked = true;
                }
                newImages.add(new ProductImage(imgDto.getUrl(), isMain, product));
            }
            // ensure single main
            if (!mainPicked && !newImages.isEmpty()) {
                newImages.get(0).setMain(true);
            }
            if (product.getImages() != null) {
                product.getImages().clear();

                product.getImages().addAll(newImages);
                return;
            }
            product.setImages(newImages);

        }
    }

    // ##############################
    // ######### Get Product ########
    // ##############################
    public ProductForCompany getProduct(Long productId) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ProductForCompany.fromProduct(product);
    }

    public Product getCompanyProductHelper(Long productId, Long companyId) {
        Product product = productRepo.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        return product;
    }

    // Ownership-scoped fetch
    public ProductForCompany getCompanyProduct(Long productId, Long companyId) {
        Product product = productRepo.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        return ProductForCompany.fromProduct(product);
    }

    // Activate/deactivate
    @Transactional
    public void setActive(Long productId, Long companyId, boolean active) {
        Product p = getCompanyProductHelper(productId, companyId);
        p.setIsActive(active);
        productRepo.save(p);
    }

    @Transactional
    public void deleteCompanyProduct(Long productId, Long companyId) {
        Product p = getCompanyProductHelper(productId, companyId);
        // Soft delete via @SQLDelete
        productRepo.delete(p);
    }

    @Transactional
    public ProductForCompany updateProduct(CreateProductDto dto, Long productId, Long companyId) {
        if (dto == null) {
            throw new IllegalArgumentException("update dto must not be null");
        }
        Product p = getCompanyProductHelper(productId, companyId);

        // Scalars: update only when provided (null or blank => do not change)
        if (dto.getName() != null && !dto.getName().isBlank()) {
            p.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            p.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            p.setPrice(dto.getPrice());
        }
        if (dto.getQuantity() != null) {
            p.setQuantity(dto.getQuantity());
        }
        if (dto.getPreparationDays() != null) {
            p.setPreparationDays(dto.getPreparationDays());
        }
        if (dto.getIsActive() != null) {
            p.setIsActive(dto.getIsActive());
        }
        if (dto.getAvailabilityStatus() != null) {
            p.setAvailabilityStatus(dto.getAvailabilityStatus());
        }

        // Relations: categories
        if (dto.getCategoryIds() != null) {
            List<Category> categories = categoryRepo.findAllById(dto.getCategoryIds());
            p.setCategories(categories);
        }

        // Tags: create or fetch only when provided
        if (dto.getTagNames() != null) {
            List<Tag> tags = tagService.getOrCreateTags(dto.getTagNames());
            p.setTags(tags);
        }

        // Images: replace only when a non-null list provided
        checkMainImage(dto, p);

        return ProductForCompany.fromProduct(productRepo.save(p));
    }

    public GetProductDto getProductDtoById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
        return new GetProductDto(product);
    }

    public List<com.hand.demo.model.repository.GetProductCardProjection> getProductCardLists(Company company) {

        List<com.hand.demo.model.repository.GetProductCardProjection> productCardDto = productRepo
                .findAllActiveProjectedByCompanyId(company.getId());
        return productCardDto;

    }

    // Get company's products for company dashboard/display by companyId
    public List<CompanyProductProjection> getCompanyProductsForDisplay(Long companyId) {
        return productRepo.retrieveProductsForCompany(companyId);
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
    // ########## Rating ###########
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

    public List<GetReviewsProjection> getRatings(Long productId, Sort sortBy) {
        Pageable pageable = PageRequest.of(0, 5, sortBy);

        Page<GetReviewsProjection> reviews = productRepo.findReviewsByProductId(productId, pageable);

        return reviews.isEmpty() ? null : reviews.getContent();
    }

}
