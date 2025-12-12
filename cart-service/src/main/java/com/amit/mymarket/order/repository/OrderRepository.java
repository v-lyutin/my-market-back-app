package com.amit.mymarket.order.repository;

import com.amit.mymarket.order.domain.entity.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

    @Query("""
           select *
           from shop.orders
           where session_id = :sessionId
           """)
    Flux<Order> findAllBySessionId(String sessionId);

    @Query("""
           select *
           from shop.orders
           where id = :orderId and session_id = :sessionId
           """)
    Mono<Order> findByIdAndSessionId(long orderId, String sessionId);

}
