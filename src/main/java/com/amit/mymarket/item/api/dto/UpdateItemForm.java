package com.amit.mymarket.item.api.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateItemForm(

        @Size(max = 255, message = "must be at most 255 characters long")
        String title,

        @Size(max = 2048, message = "must be at most 2048 characters long")
        String description,

        @Positive(message = "must be positive")
        Long priceMinor) {
}
