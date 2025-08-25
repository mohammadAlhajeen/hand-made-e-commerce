package com.hand.demo.model.entity.page;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Embeddable 
@Getter 
@Setter
public class SocialLink {
    private String platform;
    private String url;
    private Boolean show = true;
    private Integer orderIndex;
}
