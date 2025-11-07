package com.amit.mymarket.common.web.dto;

public record ItemDto(
        long id,
        String title,
        String description,
        String imagePath,
        long price,
        int count) {
}
