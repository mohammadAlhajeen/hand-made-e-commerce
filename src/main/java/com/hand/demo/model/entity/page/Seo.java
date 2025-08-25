package com.hand.demo.model.entity.page;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable 
@Getter 
@Setter
public class Seo {
    @Column(length = 70)
    private String metaTitle;
    
    @Column(length = 160)
    private String metaDescription;
    
    private String ogImageUrl;
}
