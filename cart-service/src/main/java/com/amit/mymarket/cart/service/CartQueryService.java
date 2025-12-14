package com.amit.mymarket.cart.service;

import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CartQueryService {

    /**
     * Returns all cart rows for the session’s active cart.
     * Each CartItem references Item lazily + caller decides how to initialize it.
     */
    Flux<CartItemRow> getCartItems(String sessionId);

    /**
     * Calculates the cart total (in minor units) for the session’s active cart.
     */
    Mono<Long> calculateCartTotalPrice(String sessionId);

}
