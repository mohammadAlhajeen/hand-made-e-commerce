package com.hand.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.page.PageSection;

@Repository
public interface PageSectionRepository extends JpaRepository<PageSection, Long> {
    @Query("select s from PageSection s where s.page.id = :pageId order by s.orderIndex asc, s.id asc")
    List<PageSection> findByPageOrdered(Long pageId);
}
