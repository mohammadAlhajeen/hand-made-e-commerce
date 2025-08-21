package com.hand.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.Dtos.appuser_dtos.RatingDistributionDto;
import com.hand.demo.model.Dtos.product_dtos.GetInStockProductDto;
import com.hand.demo.model.Dtos.product_dtos.GetPreOrderProductDto;
import com.hand.demo.model.Dtos.product_dtos.InStockProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.PreOrderProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.ProductDTOs;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.InStockProduct;
import com.hand.demo.model.entity.PreOrderProduct;
import com.hand.demo.model.entity.Product;
import com.hand.demo.repository.CompanyProductProjection;
import com.hand.demo.repository.GetProductCardProjection;
import com.hand.demo.repository.GetReviewsProjection;
import com.hand.demo.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepo;

    // ##############################
    // ######### Get Product ########
    // ##############################
    public ProductDTOs getProduct(Long productId) {
        var product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return switch (product) {
            case InStockProduct p -> InStockProductForCompanyV1.fromProduct(p);
            case PreOrderProduct p -> PreOrderProductForCompanyV1.fromProduct(p);
            default -> throw new IllegalArgumentException("Unknown product type: " + product.getClass());
        };
    }

    public ProductDTOs getProductForCustomer(Long productId) {
        var product = productRepo.findByIdAndIsActive(productId, true)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return switch (product) {
            case InStockProduct p -> GetInStockProductDto.fromProduct(p);
            case PreOrderProduct p -> GetPreOrderProductDto.fromProduct(p);
            default -> throw new IllegalArgumentException("Unknown product type: " + product.getClass());
        };
    }

    public Product getCompanyProductHelper(Long productId, Long companyId) {
        Product product = productRepo.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        return product;
    }

    // Ownership-scoped fetch
    public ProductDTOs getCompanyProduct(Long productId, Long companyId) {
        var product = productRepo.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return switch (product) {
            case InStockProduct p -> InStockProductForCompanyV1.fromProduct(p);
            case PreOrderProduct p -> PreOrderProductForCompanyV1.fromProduct(p);
            default -> throw new IllegalArgumentException("Unknown product type: " + product.getClass());
        };
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

    public List<com.hand.demo.repository.GetProductCardProjection> getProductCardLists(Company company) {

        List<com.hand.demo.repository.GetProductCardProjection> productCardDto = productRepo
                .findAllActiveProjectedByCompanyId(company.getId());
        return productCardDto;

    }

    // Get company's products for company dashboard/display by companyId
    public List<CompanyProductProjection> getCompanyProductsForDisplay(Long companyId) {
        System.out.println(companyId);
        return productRepo.retrieveProductsForCompany(companyId);
    }

    // Search products cards by product name (native search function)
    public List<GetProductCardProjection> searchProductCards(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName must not be blank");
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

    // ##############################
    // ######### Public APIs ########
    // ##############################

    /**
     * Get all active products with pagination and filtering
     */
    public Page<GetProductCardProjection> getPublicProducts(int page, int size, String category, String tag,
            String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // TODO: سيتم تطبيق الفلترة بعد إضافة الـ repositories المناسبة
        // هذا مجرد بداية أساسية
        return productRepo.findAllActiveProducts(pageable);
    }

    /**
     * Get public product details
     */
    public ProductDTOs getPublicProduct(Long productId) {
        return getProductForCustomer(productId); // استخدام الوظيفة الموجودة
    }

    /**
     * Search products by name
     */
    public List<GetProductCardProjection> searchProducts(String query) {
        return searchProductCards(query); // استخدام الوظيفة الموجودة
    }

    /**
     * Get featured products (أحدث 10 منتجات نشطة)
     */
    public List<GetProductCardProjection> getFeaturedProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        return productRepo.findAllActiveProducts(pageable).getContent();
    }

    /**
     * Get products by category
     */
    public List<GetProductCardProjection> getProductsByCategory(Long categoryId) {
        return productRepo.findActiveByCategoryId(categoryId);
    }

}
