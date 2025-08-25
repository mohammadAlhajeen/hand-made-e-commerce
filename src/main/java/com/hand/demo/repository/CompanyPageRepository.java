package com.hand.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.page.CompanyPage;
import com.hand.demo.model.entity.page.PageStatus;

@Repository
public interface CompanyPageRepository extends JpaRepository<CompanyPage, Long> {
    
    Optional<CompanyPage> findByCompanyIdAndSlug(Long companyId, String slug);

    boolean existsByCompanyIdAndSlugAndIdNot(Long companyId, String slug, Long excludeId);

    Optional<CompanyPage> findByCompanyIdAndSlugAndStatus(Long companyId, String slug, PageStatus published);

}
