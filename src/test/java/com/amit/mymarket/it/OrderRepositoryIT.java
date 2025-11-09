package com.amit.mymarket.it;

import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.repository.projection.OrderHeaderRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName(value = "Should return all orders for the given session id sorted by creation date descending")
    @Sql(statements = {
            "insert into shop.orders (id, session_id, status, total_minor, created_at) values (1,'session-1','CREATED',500, now() - interval '3 days')",
            "insert into shop.orders (id, session_id, status, total_minor, created_at) values (2,'session-1','PAID',700, now() - interval '2 days')",
            "insert into shop.orders (id, session_id, status, total_minor, created_at) values (3,'session-1','CANCELLED',100, now() - interval '1 day')",
            "insert into shop.orders (id, session_id, status, total_minor, created_at) values (4,'session-2','CREATED',200, now())"
    })
    void findOrdersBySession_shouldReturnAllOrdersForGivenSessionIdSortedByCreationDateDescending() {
        List<OrderHeaderRow> orders = this.orderRepository.findOrdersBySession("session-1");

        assertThat(orders).hasSize(3);
        assertThat(orders)
                .extracting(OrderHeaderRow::getId)
                .containsExactly(3L, 2L, 1L);
        assertThat(orders)
                .extracting(OrderHeaderRow::getTotalMinor)
                .containsExactly(100L, 700L, 500L);
    }

    @Test
    @DisplayName(value = "Should return an empty list when there are no orders for the given session id")
    @Sql(statements = {
            "insert into shop.orders (id, session_id, total_minor) values (10,'another-session',400)"
    })
    void findOrdersBySession_shouldReturnEmptyListWhenThereAreNoOrdersForGivenSessionId() {
        List<OrderHeaderRow> orders = this.orderRepository.findOrdersBySession("missing-session");
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName(value = "Should find order header by order id and session id when the order exists")
    @Sql(statements = {
            "insert into shop.orders (id, session_id, status, total_minor) values (20,'session-3','PAID',999)"
    })
    void findOrderHeader_shouldFindOrderHeaderByOrderIdAndSessionIdWhenOrderExists() {
        Optional<OrderHeaderRow> optionalOrder = this.orderRepository.findOrderHeader(20L, "session-3");

        assertThat(optionalOrder).isPresent();
        OrderHeaderRow order = optionalOrder.orElseThrow();
        assertThat(order.getId()).isEqualTo(20L);
        assertThat(order.getTotalMinor()).isEqualTo(999L);
    }

    @Test
    @DisplayName(value = "Should return empty optional when order with given id does not belong to provided session")
    @Sql(statements = {
            "insert into shop.orders (id, session_id, total_minor) values (21,'session-4',1000)"
    })
    void findOrderHeader_shouldReturnEmptyOptionalWhenOrderWithGivenIdDoesNotBelongToProvidedSession() {
        Optional<OrderHeaderRow> order = this.orderRepository.findOrderHeader(21L, "session-999");
        assertThat(order).isEmpty();
    }

    @Test
    @DisplayName(value = "Should return empty optional when order with given id does not exist")
    void findOrderHeader_shouldReturnEmptyOptionalWhenOrderWithGivenIdDoesNotExist() {
        Optional<OrderHeaderRow> order = this.orderRepository.findOrderHeader(9999L, "session-1");
        assertThat(order).isEmpty();
    }

}
