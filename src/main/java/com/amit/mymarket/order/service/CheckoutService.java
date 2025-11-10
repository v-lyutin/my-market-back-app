package com.amit.mymarket.order.service;

import com.amit.mymarket.order.service.exception.EmptyCartException;

public interface CheckoutService {

    /**
     * Creates an order from the session’s active cart:
     *  - snapshots title/price/quantity into orders_items,
     *  - calculates and stores total,
     *  - clears the cart,
     * and returns the new order id.
     *
     * @throws EmptyCartException if the active cart has no items
     */
    long createOrderFromActiveCartAndClear(String sessionId);

}
