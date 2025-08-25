package com.hand.demo.model.entity.page;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable 
@Getter 
@Setter
public class Theme {


    private String logoUrl;
    private String coverUrl;
}