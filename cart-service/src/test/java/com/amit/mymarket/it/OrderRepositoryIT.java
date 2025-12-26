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
                                insert into shop.orders (id, user_id, total_minor) values
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
    @DisplayName(value = "Should return all orders for given user identifier")
    void findAllByUserId_shouldReturnAllOrdersForGivenUserIdentifier() {
        String userId = "session-123";

        Flux<Order> orders = this.orderRepository.findAllByUserId(userId);

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
    @DisplayName(value = "Should return empty result when there are no orders for given user identifier")
    void findAllByUserId_shouldReturnEmptyResultWhenNoOrdersExistForGivenUserIdentifier() {
        String userId = "unknown-session";

        Flux<Order> orders = this.orderRepository.findAllByUserId(userId);

        StepVerifier.create(orders).verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return order when order with given identifier and user identifier exists")
    void findByIdAndUserId_shouldReturnOrderWhenOrderWithGivenIdentifierAndUserIdentifierExists() {
        long orderId = 2L;
        String userId = "session-123";

        Mono<Order> result = this.orderRepository.findByIdAndUserId(orderId, userId);

        StepVerifier.create(result)
                .assertNext(order -> {
                    assertThat(order.getId()).isEqualTo(2L);
                    assertThat(order.getTotalMinor()).isEqualTo(750L);
                    assertThat(order.getUserId()).isEqualTo("session-123");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when order identifier does not exist for given user identifier")
    void findByIdAndUserId_shouldReturnEmptyResultWhenOrderIdentifierDoesNotExistForGivenUserIdentifier() {
        long orderId = 999L;
        String userId = "session-123";

        Mono<Order> order = this.orderRepository.findByIdAndUserId(orderId, userId);

        StepVerifier.create(order).verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when order belongs to another user identifier")
    void findByIdAndUserId_shouldReturnEmptyResultWhenOrderBelongsToAnotherUserIdentifier() {
        long orderId = 3L;
        String userId = "session-123";

        Mono<Order> order = this.orderRepository.findByIdAndUserId(orderId, userId);

        StepVerifier.create(order).verifyComplete();
    }

}
