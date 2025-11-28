package com.amit.mymarket.order.service;

import reactor.core.publisher.Mono;

public interface CheckoutService {

    /**
     * Creates an order from the session’s active cart:
     *  - snapshots title/totalMinor/quantity into orders_items,
     *  - calculates and stores total,
     *  - clears the cart,
     * and returns the new order id.
     */
    Mono<Long> createOrderFromActiveCartAndClear(String sessionId);

}
