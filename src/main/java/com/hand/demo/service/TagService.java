package com.hand.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.model.entity.Tag;
import com.hand.demo.model.repository.TagRepository;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Finds all tags in the system
     */
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    /**
     * Finds a tag by its ID
     */
    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }
    
    /**
     * Finds tags by name list
     */
    public List<Tag> findByNames(List<String> tagNames) {
        return tagRepository.findByNameIn(tagNames);
    }

    /**
     * Creates or retrieves tags by name list
     * First creates any missing tags, then retrieves all
     * 
     * @param tagNames List of tag names to find or create
     * @return List of Tag entities
     */
    @Transactional
    public List<Tag> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Insert any new tags that don't already exist
        addTagsIfNotExists(tagNames);
        
        // Now fetch all the tags we need
        return tagRepository.findByNameIn(tagNames);
    }
    
    /**
     * Adds multiple tags if they don't already exist
     * 
     * @param tagNames List of tag names to add
     * @return Number of tags attempted to insert
     */
    @Transactional
    public int addTagsIfNotExists(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return 0;
        }
        
        // Filter blank names
        List<String> validNames = tagNames.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .distinct()
            .toList();
            
        if (validNames.isEmpty()) {
            return 0;
        }
        
        // Build dynamic INSERT IGNORE statement with placeholders
        StringBuilder sql = new StringBuilder("INSERT IGNORE INTO tags(name) VALUES ");
        for (int i = 0; i < validNames.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("(?)");
        }
        
        // Execute the query with all tag names as parameters
        return jdbcTemplate.update(sql.toString(), validNames.toArray());
    }
    
    /**
     * Adds multiple tags from JSON string array
     * 
     * @param jsonTagNames JSON string array of tag names (e.g., ["tag1","tag2"])
     * @return Number of tags inserted
     */
    @Transactional
    public int addTagsFromJson(String jsonTagNames) throws JsonProcessingException {
        if (jsonTagNames == null || jsonTagNames.isEmpty()) {
            return 0;
        }
        
        // Convert JSON to tag list and add
        List<String> tagList = objectMapper.readValue(jsonTagNames, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        return addTagsIfNotExists(tagList);
    }
    
    /**
     * Legacy method for adding tags from JSON string
     */
    @Transactional
    public void addTagsIfNotExists(List<String> newTags) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(newTags);
        tagRepository.insertIgnoreExisting(json);
    }

    /**
     * Adds a single tag
     */
    @Transactional
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }
    
    /**
     * Adds a single tag by name
     */
    @Transactional
    public int addTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return 0;
        }
        return tagRepository.insertSingleTag(tagName.trim());
    }

    /**
     * Deletes a tag by ID
     */
    @Transactional
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }
}
