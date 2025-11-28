package com.amit.mymarket.item.api.dto;

import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.service.type.SortType;

public record MutateItemForm(
        Long id,
        ItemAction action,
        String search,
        SortType sort,
        Integer pageNumber,
        Integer pageSize) {
}
