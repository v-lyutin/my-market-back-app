package com.amit.mymarket.order.repository;

import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.repository.projection.OrderHeader;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

    @Query(value = """
            select orders.id as id,
                   orders.total_minor as totalMinor
            from shop.orders
            where orders.session_id = :sessionId
            """)
    Flux<OrderHeader> findOrdersBySession(String sessionId);

    @Query(value = """
            select orders.id as id,
                   orders.total_minor as totalMinor
            from shop.orders
            where orders.id = :orderId and orders.session_id = :sessionId
            """)
    Mono<OrderHeader> findOrderHeader(long orderId, String sessionId);

}
