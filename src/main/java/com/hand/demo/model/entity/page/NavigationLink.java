package com.hand.demo.model.entity.page;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Embeddable 
@Getter 
@Setter
public class NavigationLink {
    @Column(nullable = false)
    private String label;
    
    @Column(nullable = false)
    private String href;
    
    private Integer orderIndex;
}
