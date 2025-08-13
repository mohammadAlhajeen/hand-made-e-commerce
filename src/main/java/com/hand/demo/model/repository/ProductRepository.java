package com.hand.demo.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

 
    @org.springframework.data.jpa.repository.Query("""
        SELECT p.id AS id, p.name AS name, p.description AS description, p.price AS price,
               (
                   SELECT img.url FROM ProductImage img
                   WHERE img.product = p AND img.isMain = true
               ) AS mainImageUrl,
               p.preparationDays AS preparationDays
        FROM Product p
        WHERE p.company.id = :companyId AND p.isActive = true
    """)
    List<GetProductCardProjection> findAllProjectedByCompanyId(Long companyId);
    @Query(value = """
                select * from search_products_cards(:ProductName)


            """,nativeQuery=true)
    List<GetProductCardProjection> searchPGetProductCardProjections(String ProductName);

        @org.springframework.data.jpa.repository.Query("""
        SELECT p.id AS id, p.name AS name, p.description AS description, p.price AS price,
               (
                   SELECT img.url FROM ProductImage img
                   WHERE img.product = p AND img.isMain = true
               ) AS mainImageUrl,
               p.preparationDays AS preparationDays
        FROM Product p
        WHERE p.name = :name
    """)
    GetProductCardProjection findProjectedByName(String name);
}
