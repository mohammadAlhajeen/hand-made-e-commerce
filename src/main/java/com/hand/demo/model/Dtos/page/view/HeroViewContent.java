package com.hand.demo.model.Dtos.page.view;

public record HeroViewContent(
    String kind, 
    String title, 
    String subtitle, 
    String imageUrl, 
    String ctaText, 
    String ctaHref
) implements ViewContent {
    public static HeroViewContent of(String title, String subtitle, String imageUrl, String ctaText, String ctaHref) {
        return new HeroViewContent("HERO", title, subtitle, imageUrl, ctaText, ctaHref);
    }
}
