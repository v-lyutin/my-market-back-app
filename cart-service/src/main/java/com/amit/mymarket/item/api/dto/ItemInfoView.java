package com.amit.mymarket.item.api.dto;

public record ItemInfoView(
        long id,
        String title,
        String description,
        String imagePath,
        String formatPrice,
        int quantity) {
}
