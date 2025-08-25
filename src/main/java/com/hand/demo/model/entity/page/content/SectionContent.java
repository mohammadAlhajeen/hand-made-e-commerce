package com.hand.demo.model.entity.page.content;

import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = HeroContent.class, name = "HERO"),
    @JsonSubTypes.Type(value = GridProductsContent.class, name = "GRID_PRODUCTS"),
    @JsonSubTypes.Type(value = PromoContent.class, name = "PROMO")
})
public sealed interface SectionContent permits HeroContent, GridProductsContent, PromoContent {}
