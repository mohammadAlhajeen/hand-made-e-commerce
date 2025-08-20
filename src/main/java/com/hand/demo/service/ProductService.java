package com.hand.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.Dtos.RatingDistributionDto;
import com.hand.demo.model.Dtos.product_dtos.InStockProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.PreOrderProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.ProductForCompanyV1;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.InStockProduct;
import com.hand.demo.model.entity.PreOrderProduct;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.repository.CompanyProductProjection;
import com.hand.demo.model.repository.GetProductCardProjection;
import com.hand.demo.model.repository.GetReviewsProjection;
import com.hand.demo.model.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepo;

    // ##############################
    // ######### Get Product ########
    // ##############################
    public ProductForCompanyV1 getProduct(Long productId) {
        var product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return switch (product) {
            case InStockProduct p -> InStockProductForCompanyV1.fromProduct(p);
            case PreOrderProduct p -> PreOrderProductForCompanyV1.fromProduct(p);
            default -> throw new IllegalArgumentException("Unknown product type: " + product.getClass());
        };
    }

    public ProductForCompanyV1 getProductForCustomer(Long productId) {
        var product = productRepo.findByIdAndIsActive(productId, true)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return switch (product) {
            case InStockProduct p -> InStockProductForCompanyV1.fromProduct(p);
            case PreOrderProduct p -> PreOrderProductForCompanyV1.fromProduct(p);
            default -> throw new IllegalArgumentException("Unknown product type: " + product.getClass());
        };
    }

    public Product getCompanyProductHelper(Long productId, Long companyId) {
        Product product = productRepo.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        return product;
    }

    // Ownership-scoped fetch
    public ProductForCompanyV1 getCompanyProduct(Long productId, Long companyId) {
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

    public List<com.hand.demo.model.repository.GetProductCardProjection> getProductCardLists(Company company) {

        List<com.hand.demo.model.repository.GetProductCardProjection> productCardDto = productRepo
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

}
