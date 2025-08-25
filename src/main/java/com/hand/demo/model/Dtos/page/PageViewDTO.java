package com.hand.demo.model.Dtos.page;

import java.time.Instant;
import java.util.List;

public record PageViewDTO(
                Long id,
                String slug,
                String title,
           
                String status,
    
                Instant updatedAt,
                Instant publishedAt,
                ThemeDTO theme,
                SeoDTO seo,
                List<NavigationLinkDTO> navigation,
                List<SocialLinkDTO> social,
                List<GetSectionDTO> sections) {
}
