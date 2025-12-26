package com.amit.mymarket.cart.service;

import com.amit.mymarket.cart.repository.projection.CartItemRow;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartQueryService {

    /**
     * Returns all cart rows for the user’s active cart.
     * Each CartItem references Item lazily + caller decides how to initialize it.
     */
    Flux<CartItemRow> getCartItems(String userId);

    /**
     * Calculates the cart total (in minor units) for the user’s active cart.
     */
    Mono<Long> calculateCartTotalPrice(String userId);

}
