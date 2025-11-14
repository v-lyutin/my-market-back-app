package com.amit.mymarket.order.service;

import com.amit.mymarket.order.domain.entity.Order;

import java.util.List;

public interface OrderQueryService {

    /**
     * Returns all orders for the given session ordered by creation date descending.
     */
    List<Order> fetchOrdersBySession(String sessionId);

    /**
     * Returns a single order if it belongs to the session + otherwise throws ResourceNotFoundException.
     */
    Order fetchOrderByIdForSession(long orderId, String sessionId);

}
