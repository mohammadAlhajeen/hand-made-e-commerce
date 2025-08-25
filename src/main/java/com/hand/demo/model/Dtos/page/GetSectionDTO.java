package com.hand.demo.model.Dtos.page;

import com.hand.demo.model.Dtos.page.view.ViewContent;

public record GetSectionDTO(
        String type,
        Integer orderIndex,
        ViewContent content
) {
}
