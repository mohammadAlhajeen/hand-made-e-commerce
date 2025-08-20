package com.hand.demo.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.InStockProduct;

@Repository
public interface InStockProductRepository extends JpaRepository<InStockProduct, Long> {

    @org.springframework.data.jpa.repository.Query("""
                SELECT p.id AS id, p.name AS name, p.description AS description, p.price AS price,
                                              (
                            SELECT media.absoluteUrl FROM ProductImage img
                            LEFT JOIN img.media media
                            WHERE img.product = p AND img.main = true
                            ORDER BY img.id ASC
                       )
                       AS mainImageUrl,
                       p.preparationDays AS preparationDays,
                       p.averageRating AS averageRating
                FROM Product p
                WHERE p.company.id = :companyId AND p.isActive = true
            """)
    List<GetProductCardProjection> findAllActiveProjectedByCompanyId(Long companyId);

    @Query("""
                SELECT p.id AS id, p.name AS name,
                                             (
                            SELECT media.absoluteUrl FROM ProductImage img
                            LEFT JOIN img.media media
                            WHERE img.product = p AND img.main = true
                            ORDER BY img.id ASC
                       )
                       AS mainImageUrl,
             
                       p.preparationDays AS preparationDays
                FROM Product p
                WHERE p.company.id = :companyId
                ORDER BY p.isActive DESC, p.createdAt ASC
            """)
    List<CompanyProductProjection> retrieveProductsForCompany(Long companyId);

    @Query(value = "SELECT * FROM search_products_cards(:productName)", nativeQuery = true)
    List<GetProductCardProjection> searchGetProductCardProjections(@Param("productName") String productName);

    @org.springframework.data.jpa.repository.Query("""
                SELECT p.id AS id, p.name AS name, p.description AS description, p.price AS price,
                                           (
                            SELECT media.absoluteUrl FROM ProductImage img
                            LEFT JOIN img.media media
                            WHERE img.product = p AND img.main = true
                            ORDER BY img.id ASC
                       )
                       AS mainImageUrl,
                    p.preparationDays AS preparationDays
                FROM Product p
                WHERE p.name = :name
            """)
    GetProductCardProjection findProjectedByName(String name);

    // Ownership checks and scoped fetches
    java.util.Optional<InStockProduct> findByIdAndCompanyId(Long id, Long companyId);


    @Query("""
            SELECT r.id AS id, r.rating AS rating, r.shortComment AS shortComment, r.createdAt AS createdAt,
                r.product.id AS productId, r.customer.id AS customerId, r.customer.name AS customerName
            FROM Review r
            WHERE r.product.id = :productId
            """)
    org.springframework.data.domain.Page<GetReviewsProjection> findReviewsByProductId(
            @Param("productId") Long productId,
            org.springframework.data.domain.Pageable pageable);

}
