package com.amit.mymarket.order.service;

import com.amit.mymarket.order.service.model.CheckoutAvailability;
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

    Mono<CheckoutAvailability> getCheckoutAvailability(String sessionId);


}
