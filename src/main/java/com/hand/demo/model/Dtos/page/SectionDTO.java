package com.hand.demo.model.Dtos.page;

import com.hand.demo.model.entity.page.content.SectionContent;

public record SectionDTO(
        String type,
        Integer orderIndex,
        SectionContent content
) {
}
