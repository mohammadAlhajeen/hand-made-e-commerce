package com.hand.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.InStockProduct;

import jakarta.persistence.LockModeType;

@Repository
public interface InStockProductRepository extends JpaRepository<InStockProduct, Long> {
    Optional<InStockProduct> findByIdAndCompanyId(Long id, Long companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select i from InStockProduct i
                where i.id = :productId and i.company.id = :companyId
            """)
    Optional<InStockProduct> lockByIdAndCompanyId(@Param("productId") Long productId,
            @Param("companyId") Long companyId);
}
