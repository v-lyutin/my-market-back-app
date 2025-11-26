package com.amit.mymarket.order.service;

import com.amit.mymarket.order.domain.entity.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderQueryService {

    /**
     * Returns all orders for the given session ordered by creation date descending.
     */
    Flux<Order> getOrdersBySession(String sessionId);

    /**
     * Returns a single order if it belongs to the session + otherwise throws ResourceNotFoundException.
     */
    Mono<Order> getOrderByIdForSession(long orderId, String sessionId);

}
