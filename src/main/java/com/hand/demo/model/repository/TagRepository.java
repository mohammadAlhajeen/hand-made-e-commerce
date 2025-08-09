package com.hand.demo.model.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // Simple single tag insert (MySQL compatible)
    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO tags(name) VALUES (:name)", nativeQuery = true)
    int insertSingleTag(@Param("name") String name);
    
    // Insert list of tags with a single query (MySQL compatible)
    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO tags(name) VALUES (:name1), (:name2), (:name3)", nativeQuery = true)
    int insertMultipleTags(@Param("name1") String name1, @Param("name2") String name2, @Param("name3") String name3);

    // Insert a list of tags directly (better performance)
    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO tags(name) VALUES(:names)", nativeQuery = true)
    int insertBulkTags(@Param("names") List<String> names);
    
    // Helper method using JdbcTemplate for truly dynamic insertion of any number of tags
    @Transactional
    default int insertTagsList(List<String> tagNames, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        if (tagNames == null || tagNames.isEmpty()) return 0;
        
        // Filter out empty strings and trim
        List<String> validNames = tagNames.stream()
            .filter(name -> name != null && !name.trim().isEmpty())
            .map(String::trim)
            .distinct()
            .toList();
        
        if (validNames.isEmpty()) return 0;
        
        // Build dynamic SQL for multiple VALUES
        StringBuilder sql = new StringBuilder("INSERT IGNORE INTO tags(name) VALUES ");
        for (int i = 0; i < validNames.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("(?)");
        }
        
        // Execute the dynamic query
        return jdbcTemplate.update(
            sql.toString(),
            validNames.toArray()
        );
    }
    
    // Helper method for inserting multiple tags from JSON string
    @Transactional
    default int insertIgnoreExisting(String jsonNames) {
        if (jsonNames == null || jsonNames.isEmpty()) return 0;
        
        // Parse the JSON array manually since JSON_TABLE causes syntax errors
        String json = jsonNames.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return 0; // Not a valid JSON array
        }
        
        // Extract names from JSON array: ["tag1","tag2"] -> tag1,tag2
        String content = json.substring(1, json.length() - 1);
        String[] parts = content.split(",");
        int count = 0;
        
        for (String part : parts) {
            String name = part.trim();
            if (name.startsWith("\"") && name.endsWith("\"")) {
                name = name.substring(1, name.length() - 1);
            }
            if (!name.isEmpty()) {
                count += insertSingleTag(name);
            }
        }
        
        return count;
    }
    
    List<Tag> findByNameIn(Collection<String> names);
}
