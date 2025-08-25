package com.hand.demo.model.entity.page.content;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record HeroContent(
    @NotBlank String title,
    String subtitle,
    @NotNull UUID imageMediaId,   // نحلّه إلى URL عند الإخراج
    @NotBlank String ctaText,
    @NotBlank String ctaHref
) implements SectionContent {}
