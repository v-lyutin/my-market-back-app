package com.amit.mymarket.order.api.dto;

import java.util.List;

public record OrderDto(
        long id,
        List<OrderItemDto> items,
        String totalFormatted) {
}
