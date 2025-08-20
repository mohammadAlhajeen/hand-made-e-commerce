package com.hand.demo.model.Dtos;

import java.util.UUID;

public record CreateImageDto(UUID imageId, boolean main, Integer sortOrder) {
}