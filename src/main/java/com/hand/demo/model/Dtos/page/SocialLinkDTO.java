package com.hand.demo.model.Dtos.page;

public record SocialLinkDTO(
        String platform,
        String url,
        Boolean show,
        Integer orderIndex) {
}
