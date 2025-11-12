package com.amit.mymarket.item.api.dto;

public record ItemInfoView(
        long id,
        String title,
        String description,
        String imagePath,
        long priceMinor,
        int count) {
}
