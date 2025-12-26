package com.amit.mymarket.order.repository;

import com.amit.mymarket.order.domain.entity.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

    @Query(value = """
            select *
            from shop.orders
            where user_id = :userId
            """)
    Flux<Order> findAllByUserId(String userId);

    @Query(value = """
            select *
            from shop.orders
            where id = :orderId and user_id = :userId
            """)
    Mono<Order> findByIdAndUserId(long orderId, String userId);

}
