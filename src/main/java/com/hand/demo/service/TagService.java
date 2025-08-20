package com.hand.demo.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private JdbcTemplate jdbc;

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

    @Transactional
    public List<Tag> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        List<String> names = tagNames.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        if (names.isEmpty())
            return List.of();
        jdbc.batchUpdate(
                "INSERT INTO tags(name) VALUES (?) ON CONFLICT (name) DO NOTHING",
                names,
                100,
                (ps, name) -> ps.setString(1, name));
        return tagRepository.findAllByNameIn(names);
    }

}
