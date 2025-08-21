package com.hand.demo.model.Dtos.image_dtos;

import java.util.UUID;

public record CreateImageDto(UUID imageId, boolean main, Integer sortOrder) {
}