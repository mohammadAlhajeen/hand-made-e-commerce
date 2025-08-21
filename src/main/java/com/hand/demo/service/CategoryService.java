package com.hand.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.Category;
import com.hand.demo.repository.CategoryRepository;
import com.hand.demo.repository.getCategoryProjection;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category findById(Long id) {
        return categoryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    // ##############################
    // ######### Public APIs ########
    // ##############################

    /**
     * Get all active categories for public display
     */
    public List<getCategoryProjection> getAllActiveCategories() {
        return categoryRepository.findAllProjections();
    }

    public List<getCategoryProjection> getAllParentCategories() {
        return categoryRepository.findAllParentProjections();
    }

    public List<getCategoryProjection> getAllChildCategories(Long parentId) {
        return categoryRepository.findAllChildProjections(parentId);
    }

}
