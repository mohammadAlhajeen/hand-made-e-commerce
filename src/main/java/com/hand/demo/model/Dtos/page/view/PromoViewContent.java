package com.hand.demo.model.Dtos.page.view;

public record PromoViewContent(
        String kind,
        String title,
        String badge,
        String href) implements ViewContent {
    public static PromoViewContent of(String title, String badge, String href) {
        return new PromoViewContent("PROMO", title, badge, href);
    }
}
