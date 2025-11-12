package com.amit.mymarket.item.api.dto;

import com.amit.mymarket.common.util.Paging;
import com.amit.mymarket.item.domain.type.SortType;

import java.util.List;

public record CatalogPageDto(
        List<List<ItemInfoView>> items,
        Paging paging,
        String search,
        SortType sort) {
}
