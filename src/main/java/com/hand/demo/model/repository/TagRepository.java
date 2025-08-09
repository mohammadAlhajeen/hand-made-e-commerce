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

    // Helper method for inserting multiple tags
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
