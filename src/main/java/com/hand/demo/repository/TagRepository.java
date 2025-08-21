package com.hand.demo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hand.demo.model.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByNameIn(Collection<String> names);
}
