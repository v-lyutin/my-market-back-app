package com.amit.mymarket.cart.service;

public interface CartCommandService {

    /**
     * Increments item quantity by 1 in the active cart of the session (creates item row if absent).
     */
    void incrementCartItemQuantity(String sessionId, long itemId);

    /**
     * Decrements item quantity by 1 + deletes row when quantity would reach zero (no-op if absent).
     */
    void decrementCartItemQuantityOrDelete(String sessionId, long itemId);

    /**
     * Deletes item row from cart regardless of the current quantity (no-op if absent).
     */
    void deleteCartItem(String sessionId, long itemId);

    /**
     * Clears the active cart (no-op if cart is missing/empty).
     */
    void clearActiveCart(String sessionId);

}
