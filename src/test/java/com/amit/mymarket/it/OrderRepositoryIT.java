package com.amit.mymarket.it;

import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderRepository integration tests")
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        statements = {
                // session-123
                "insert into shop.orders (id, session_id, total_minor) values (1, 'session-123', 500);",
                "insert into shop.orders (id, session_id, total_minor) values (2, 'session-123', 750);",

                // another-session
                "insert into shop.orders (id, session_id, total_minor) values (3, 'another-session', 1000);"
        }
)
@Sql(
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
        statements = {
                "delete from shop.orders;"
        }
)
class OrderRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private OrderRepository orderRepository;

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
