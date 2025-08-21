package com.hand.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c.id AS id, c.name AS name,CASE WHEN EXISTS (SELECT 1 FROM Category child WHERE child.parent = c ) THEN true ELSE false END AS hasChildren  FROM Category c ")
    public List<getCategoryProjection> findAllProjections();

    @Query("SELECT c.id AS id, c.name AS name,CASE WHEN EXISTS (SELECT 1 FROM Category child WHERE child.parent = c ) THEN true ELSE false END AS hasChildren FROM Category c WHERE c.parent IS NULL")
    public List<getCategoryProjection> findAllParentProjections();

    @Query("SELECT c.id AS id, c.name AS name ,CASE WHEN EXISTS (SELECT 1 FROM Category child WHERE child.parent = c ) THEN true ELSE false END AS hasChildren FROM Category c WHERE c.parent.id = :parentId")
    public List<getCategoryProjection> findAllChildProjections(Long parentId);

}
