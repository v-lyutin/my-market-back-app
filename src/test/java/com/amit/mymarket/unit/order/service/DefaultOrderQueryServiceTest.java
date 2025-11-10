package com.amit.mymarket.unit.order.service;

import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.order.entity.Order;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.repository.projection.OrderHeaderRow;
import com.amit.mymarket.order.service.impl.DefaultOrderQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class DefaultOrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DefaultOrderQueryService orderQueryService;

    @Test
    @DisplayName(value = "Should return empty list when there are no orders for session")
    void fetchOrdersBySession_shouldReturnEmptyListWhenNoOrdersForSession() {
        String sessionId = "session-0";
        when(this.orderRepository.findOrdersBySession(sessionId)).thenReturn(Collections.emptyList());

        List<Order> result = this.orderQueryService.fetchOrdersBySession(sessionId);

        assertThat(result).isEmpty();
        verify(this.orderRepository).findOrdersBySession(sessionId);
        verify(this.orderRepository, never()).findAllById(anyCollection());
    }

    @Test
    @DisplayName(value = "Should return orders in the same order as headers and skip missing ones")
    void fetchOrdersBySession_shouldReturnOrdersInHeaderOrderAndSkipMissingOnes() {
        String sessionId = "session-1";

        OrderHeaderRow orderHeaderRow1 = mock(OrderHeaderRow.class);
        when(orderHeaderRow1.getId()).thenReturn(1L);
        OrderHeaderRow orderHeaderRow2 = mock(OrderHeaderRow.class);
        when(orderHeaderRow2.getId()).thenReturn(2L);
        OrderHeaderRow orderHeaderRow3 = mock(OrderHeaderRow.class);
        when(orderHeaderRow3.getId()).thenReturn(3L);

        when(this.orderRepository.findOrdersBySession(sessionId)).thenReturn(List.of(orderHeaderRow3, orderHeaderRow1, orderHeaderRow2));

        Order order1 = mock(Order.class);
        when(order1.getId()).thenReturn(1L);
        Order order3 = mock(Order.class);
        when(order3.getId()).thenReturn(3L);
        when(this.orderRepository.findAllById(List.of(3L, 1L, 2L))).thenReturn(List.of(order1, order3));

        List<Order> result = this.orderQueryService.fetchOrdersBySession(sessionId);

        assertThat(result).containsExactly(order3, order1);
        verify(this.orderRepository).findOrdersBySession(sessionId);
        verify(this.orderRepository).findAllById(List.of(3L, 1L, 2L));
    }

    @Test
    @DisplayName(value = "Should throw when order header is not found for session")
    void fetchOrderByIdForSession_shouldThrowWhenHeaderNotFound() {
        long orderId = 10L;
        String sessionId = "session-2";
        when(this.orderRepository.findOrderHeader(orderId, sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.orderQueryService.fetchOrderByIdForSession(orderId, sessionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found for session");
        verify(this.orderRepository).findOrderHeader(orderId, sessionId);
        verify(this.orderRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName(value = "Should return order entity when header exists and entity is present")
    void fetchOrderByIdForSession_shouldReturnOrderWhenHeaderAndEntityPresent() {
        long orderId = 11L;
        String sessionId = "session-3";

        OrderHeaderRow header = mock(OrderHeaderRow.class);
        when(this.orderRepository.findOrderHeader(orderId, sessionId)).thenReturn(Optional.of(header));

        Order entity = mock(Order.class);
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.of(entity));

        Order result = this.orderQueryService.fetchOrderByIdForSession(orderId, sessionId);

        assertThat(result).isSameAs(entity);
        verify(this.orderRepository).findOrderHeader(orderId, sessionId);
        verify(this.orderRepository).findById(orderId);
    }

    @Test
    @DisplayName(value = "Should throw when header exists but order entity disappeared")
    void fetchOrderByIdForSession_shouldThrowWhenOrderDisappearedAfterHeaderFound() {
        long orderId = 12L;
        String sessionId = "session-4";

        OrderHeaderRow orderHeaderRow = mock(OrderHeaderRow.class);
        when(this.orderRepository.findOrderHeader(orderId, sessionId)).thenReturn(Optional.of(orderHeaderRow));
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.orderQueryService.fetchOrderByIdForSession(orderId, sessionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order disappeared");
        verify(this.orderRepository).findOrderHeader(orderId, sessionId);
        verify(this.orderRepository).findById(orderId);
    }

}
