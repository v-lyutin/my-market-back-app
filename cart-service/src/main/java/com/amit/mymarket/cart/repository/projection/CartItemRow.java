package com.amit.mymarket.cart.repository.projection;

public record CartItemRow(
    Long id,
    String title,
    String description,
    String imagePath,
    Long priceMinor,
    Integer quantity) {
}
