package com.amit.mymarket.order.web.dto;

public record OrderItemDto(
        long id,
        String title,
        long price,
        int count) {
}
