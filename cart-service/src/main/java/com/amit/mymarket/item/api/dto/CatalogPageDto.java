package com.amit.mymarket.item.api.dto;

import com.amit.mymarket.item.service.type.SortType;

import java.util.List;

public record CatalogPageDto(
        List<List<ItemInfoView>> items,
        Paging paging,
        String searchQuery,
        SortType sort) {
}
