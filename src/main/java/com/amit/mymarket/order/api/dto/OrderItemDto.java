package com.amit.mymarket.order.api.dto;

public record OrderItemDto(
        long id,
        String title,
        String formatPrice,
        int count) {
}
