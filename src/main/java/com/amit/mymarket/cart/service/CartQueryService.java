package com.amit.mymarket.cart.service;

import com.amit.mymarket.cart.domain.entity.CartItem;

import java.util.List;

public interface CartQueryService {

    /**
     * Returns all cart rows for the session’s active cart.
     * Each CartItem references Item lazily + caller decides how to initialize it.
     */
    List<CartItem> fetchCartItems(String sessionId);

    /**
     * Calculates the cart total (in minor units) for the session’s active cart.
     */
    long calculateCartTotalMinor(String sessionId);

}
