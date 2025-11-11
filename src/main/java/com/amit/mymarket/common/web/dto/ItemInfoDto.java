package com.amit.mymarket.common.web.dto;

public record ItemInfoDto(
        long id,
        String title,
        String description,
        String imagePath,
        long price,
        int count) {
}
