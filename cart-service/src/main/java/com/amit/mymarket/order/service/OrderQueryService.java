package com.amit.mymarket.order.service;

import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderQueryService {

    /**
     * Returns all orders for the given user ordered by creation date descending.
     */
    Flux<Order> getOrdersByUserId(String userId);

    /**
     * Returns a single order if it belongs to the user + otherwise throws ResourceNotFoundException.
     */
    Mono<Order> getOrderByIdForUserId(long orderId, String userId);

    /**
     * Returns all items for a given order.
     */
    Flux<OrderItem> getOrderItems(long orderId);

}
