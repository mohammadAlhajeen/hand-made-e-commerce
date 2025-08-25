package com.hand.demo.model.entity.page.content;

import jakarta.validation.constraints.*;

public record PromoContent(
    @NotBlank String title,
    @NotBlank String badge, // "-15%"
    String href
) implements SectionContent {}
