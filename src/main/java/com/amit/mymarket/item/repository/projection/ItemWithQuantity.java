package com.amit.mymarket.item.repository.projection;

public record ItemWithQuantity(
    Long id,
    String title,
    String description,
    String imagePath,
    Long priceMinor,
    Integer quantity) {
}
