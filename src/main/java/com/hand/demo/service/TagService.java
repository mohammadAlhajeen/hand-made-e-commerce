package com.hand.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.Tag;
import com.hand.demo.model.repository.TagRepository;

@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }
    
    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }
    
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }
    
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }
}
