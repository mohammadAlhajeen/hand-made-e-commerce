package com.hand.demo.model.Dtos.page;

import java.util.List;

public record PageUpdateDTO(
                String slug,
                String title,
                ThemeDTO theme,
                SeoDTO seo,
                List<NavigationLinkDTO> navigation,
                List<SocialLinkDTO> social,
                List<SectionDTO> sections) {
}
