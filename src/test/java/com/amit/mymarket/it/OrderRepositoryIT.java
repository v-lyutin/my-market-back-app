package com.amit.mymarket.it;

import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.repository.projection.OrderHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName(value = "OrderRepository integration tests")
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
    void findOrdersBySession_shouldReturnAllOrdersForGivenSessionIdentifier() {
        String sessionId = "session-123";

        Flux<OrderHeader> orderHeaders = this.orderRepository.findOrdersBySession(sessionId);

        StepVerifier.create(orderHeaders.collectList())
                .assertNext(orderHeaderRowList -> {
                    assertThat(orderHeaderRowList).hasSize(2);

                    List<Long> orderIds = orderHeaderRowList.stream()
                            .map(OrderHeader::id)
                            .toList();

                    List<Long> totalPrices = orderHeaderRowList.stream()
                            .map(OrderHeader::totalMinor)
                            .toList();

                    assertThat(orderIds)
                            .containsExactlyInAnyOrder(1L, 2L);

                    assertThat(totalPrices)
                            .containsExactlyInAnyOrder(500L, 750L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when there are no orders for given session identifier")
    void findOrdersBySession_shouldReturnEmptyResultWhenNoOrdersExistForGivenSessionIdentifier() {
        String sessionId = "unknown-session";

        Flux<OrderHeader> orderHeaders = this.orderRepository.findOrdersBySession(sessionId);

        StepVerifier.create(orderHeaders).verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return order header when order with given identifier and session identifier exists")
    void findOrderHeader_shouldReturnOrderHeaderWhenOrderWithGivenIdentifierAndSessionIdentifierExists() {
        long orderId = 2L;
        String sessionId = "session-123";

        Mono<OrderHeader> orderHeader = this.orderRepository.findOrderHeader(orderId, sessionId);

        StepVerifier.create(orderHeader)
                .assertNext(orderHeaderRow -> {
                    assertThat(orderHeaderRow.id()).isEqualTo(2L);
                    assertThat(orderHeaderRow.totalMinor()).isEqualTo(750L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when order identifier does not exist for given session identifier")
    void findOrderHeader_shouldReturnEmptyResultWhenOrderIdentifierDoesNotExistForGivenSessionIdentifier() {
        long orderId = 999L;
        String sessionId = "session-123";

        Mono<OrderHeader> orderHeader = this.orderRepository.findOrderHeader(orderId, sessionId);

        StepVerifier.create(orderHeader).verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when order belongs to another session identifier")
    void findOrderHeader_shouldReturnEmptyResultWhenOrderBelongsToAnotherSessionIdentifier() {
        long orderId = 3L;
        String sessionId = "session-123";

        Mono<OrderHeader> orderHeader = this.orderRepository.findOrderHeader(orderId, sessionId);

        StepVerifier.create(orderHeader).verifyComplete();
    }

}
