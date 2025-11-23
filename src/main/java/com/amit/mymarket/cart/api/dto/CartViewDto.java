package com.amit.mymarket.cart.api.dto;

import com.amit.mymarket.item.api.dto.ItemInfoView;

import java.util.List;

public record CartViewDto(
        List<ItemInfoView> items,
        String totalFormatted) {
}
