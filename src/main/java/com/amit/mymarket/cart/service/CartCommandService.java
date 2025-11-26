package com.amit.mymarket.cart.service;

import reactor.core.publisher.Mono;

public interface CartCommandService {

    /**
     * Increments item quantity by 1 in the active cart of the session (creates item row if absent).
     */
    Mono<Void> incrementCartItemQuantity(String sessionId, long itemId);

    /**
     * Decrements item quantity by 1 + deletes row when quantity would reach zero (no-op if absent).
     */
    Mono<Void> decrementCartItemQuantityOrDelete(String sessionId, long itemId);

    /**
     * Deletes item row from cart regardless of the current quantity (no-op if absent).
     */
    Mono<Void> deleteCartItem(String sessionId, long itemId);

    /**
     * Clears the active cart (no-op if cart is missing/empty).
     */
    Mono<Void> clearActiveCart(String sessionId);

}
