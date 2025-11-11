package com.amit.mymarket.item.api.dto;

public record ItemView(
        long id,
        String title,
        String description,
        String imagePath,
        long priceMinor) {
}
