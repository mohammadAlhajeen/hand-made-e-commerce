package com.hand.demo.model.Dtos.page.view;

import java.util.List;

public record GridProductsViewContent(
    String kind, 
    String title, 
    Integer columns, 
    List<ProductCardDTO> items
) implements ViewContent {
    public static GridProductsViewContent of(String title, Integer columns, List<ProductCardDTO> items) {
        return new GridProductsViewContent("GRID_PRODUCTS", title, columns, items);
    }
}
