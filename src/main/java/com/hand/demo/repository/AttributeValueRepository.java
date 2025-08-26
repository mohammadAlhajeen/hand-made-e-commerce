package com.hand.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.AttributeValue;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    @Query("SELECT SUM(av.extraPrice) FROM AttributeValue av WHERE av.id IN :ids")
    BigDecimal sumExtra(@Param("ids") List<Long> ids);
    @Query("SELECT av.extraPrice FROM AttributeValue av WHERE av.id = :id")
    BigDecimal findExtraPriceById(@Param("id") Long id);
}
