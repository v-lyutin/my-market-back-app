package com.amit.mymarket.it;

import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUpTestData() {
        Mono<Void> setupFlow = this.databaseClient.sql("delete from shop.orders_items")
                .fetch()
                .rowsUpdated()
                .then(this.databaseClient.sql("delete from shop.orders")
                        .fetch()
                        .rowsUpdated())
                .then(this.databaseClient.sql("""
                                insert into shop.orders (id, session_id, total_minor) values
                                (1, 'session-123', 500),
                                (2, 'session-123', 750),
                                (3, 'another-session', 1000)
                                """)
                        .fetch()
                        .rowsUpdated())
                .then();
        setupFlow.block();
    }

    @AfterEach
    void cleanUpTestData() {
        Mono<Void> cleanupFlow = this.databaseClient.sql("delete from shop.orders_items")
                .fetch()
                .rowsUpdated()
                .then(this.databaseClient.sql("delete from shop.orders")
                        .fetch()
                        .rowsUpdated())
                .then();

        cleanupFlow.block();
    }

    @Test
    @DisplayName(value = "Should return all orders for given session identifier")
    void findAllBySessionId_shouldReturnAllOrdersForGivenSessionIdentifier() {
        String sessionId = "session-123";

        Flux<Order> orders = this.orderRepository.findAllBySessionId(sessionId);

        StepVerifier.create(orders.collectList())
                .assertNext(orderList -> {
                    assertThat(orderList).hasSize(2);

                    List<Long> orderIds = orderList.stream()
                            .map(Order::getId)
                            .toList();

                    List<Long> totalMinors = orderList.stream()
                            .map(Order::getTotalMinor)
                            .toList();

                    assertThat(orderIds).containsExactlyInAnyOrder(1L, 2L);
                    assertThat(totalMinors).containsExactlyInAnyOrder(500L, 750L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when there are no orders for given session identifier")
    void findAllBySessionId_shouldReturnEmptyResultWhenNoOrdersExistForGivenSessionIdentifier() {
        String sessionId = "unknown-session";

        Flux<Order> orders = this.orderRepository.findAllBySessionId(sessionId);

        StepVerifier.create(orders).verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return order when order with given identifier and session identifier exists")
    void findByIdAndSessionId_shouldReturnOrderWhenOrderWithGivenIdentifierAndSessionIdentifierExists() {
        long orderId = 2L;
        String sessionId = "session-123";

        Mono<Order> result = this.orderRepository.findByIdAndSessionId(orderId, sessionId);

        StepVerifier.create(result)
                .assertNext(order -> {
                    assertThat(order.getId()).isEqualTo(2L);
                    assertThat(order.getTotalMinor()).isEqualTo(750L);
                    assertThat(order.getSessionId()).isEqualTo("session-123");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when order identifier does not exist for given session identifier")
    void findByIdAndSessionId_shouldReturnEmptyResultWhenOrderIdentifierDoesNotExistForGivenSessionIdentifier() {
        long orderId = 999L;
        String sessionId = "session-123";

        Mono<Order> order = this.orderRepository.findByIdAndSessionId(orderId, sessionId);

        StepVerifier.create(order).verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when order belongs to another session identifier")
    void findByIdAndSessionId_shouldReturnEmptyResultWhenOrderBelongsToAnotherSessionIdentifier() {
        long orderId = 3L;
        String sessionId = "session-123";

        Mono<Order> order = this.orderRepository.findByIdAndSessionId(orderId, sessionId);

        StepVerifier.create(order).verifyComplete();
    }

}
