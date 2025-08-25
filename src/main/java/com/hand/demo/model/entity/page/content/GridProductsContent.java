package com.hand.demo.model.entity.page.content;

import jakarta.validation.constraints.*;
import java.util.List;

public record GridProductsContent(
    @NotBlank String title,
    @Min(1) @Max(6) Integer columns,
    @NotEmpty List<Long> productIds
) implements SectionContent {}
