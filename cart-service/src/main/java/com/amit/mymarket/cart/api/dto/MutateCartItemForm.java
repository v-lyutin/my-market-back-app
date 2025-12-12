package com.amit.mymarket.cart.api.dto;

import com.amit.mymarket.cart.api.type.CartAction;

public record MutateCartItemForm(
        Long id,
        CartAction action) {
}
