package com.amit.mymarket.order.web.dto;

import java.util.List;

public record OrderDto(
        long id,
        List<OrderItemDto> items,
        long totalSum) {
}
