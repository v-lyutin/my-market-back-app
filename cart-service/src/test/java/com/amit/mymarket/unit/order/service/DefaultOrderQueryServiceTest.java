package com.amit.mymarket.unit.order.service;

import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.exception.ServiceException;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.service.impl.DefaultOrderQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class DefaultOrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DefaultOrderQueryService orderQueryService;

    @Test
    @DisplayName(value = "Should return orders for given session identifier")
    void getOrdersBySession_shouldReturnOrdersForGivenSessionIdentifier() {
        String sessionId = "session-123";

        Order firstOrder = new Order();
        firstOrder.setId(1L);
        firstOrder.setSessionId(sessionId);

        Order secondOrder = new Order();
        secondOrder.setId(2L);
        secondOrder.setSessionId(sessionId);

        when(this.orderRepository.findAllBySessionId(sessionId)).thenReturn(Flux.just(firstOrder, secondOrder));

        Flux<Order> orders = this.orderQueryService.getOrdersBySession(sessionId);

        StepVerifier.create(orders.collectList())
                .assertNext(orderList -> {
                    assertEquals(2, orderList.size());
                    assertEquals(1L, orderList.get(0).getId());
                    assertEquals(2L, orderList.get(1).getId());
                })
                .verifyComplete();

        verify(this.orderRepository, times(1)).findAllBySessionId(sessionId);
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty for getOrdersBySession")
    void getOrdersBySession_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = "   ";

        Flux<Order> orders = this.orderQueryService.getOrdersBySession(sessionId);

        StepVerifier.create(orders)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.orderRepository);
    }

    @Test
    @DisplayName(value = "Should return order when order exists for given identifier and session identifier")
    void getOrderByIdForSession_shouldReturnOrderWhenExistsForSession() {
        long orderId = 10L;
        String sessionId = "session-123";

        Order order = new Order();
        order.setId(orderId);
        order.setSessionId(sessionId);

        when(this.orderRepository.findByIdAndSessionId(orderId, sessionId)).thenReturn(Mono.just(order));

        Mono<Order> result = this.orderQueryService.getOrderByIdForSession(orderId, sessionId);

        StepVerifier.create(result)
                .assertNext(foundOrder -> {
                    assertEquals(orderId, foundOrder.getId());
                    assertEquals(sessionId, foundOrder.getSessionId());
                })
                .verifyComplete();

        verify(this.orderRepository, times(1)).findByIdAndSessionId(orderId, sessionId);
    }

    @Test
    @DisplayName(value = "Should throw ResourceNotFoundException when order does not exist for session identifier")
    void getOrderByIdForSession_shouldThrowResourceNotFoundExceptionWhenOrderDoesNotExistForSession() {
        long orderId = 10L;
        String sessionId = "session-123";

        when(this.orderRepository.findByIdAndSessionId(orderId, sessionId)).thenReturn(Mono.empty());

        Mono<Order> order = this.orderQueryService.getOrderByIdForSession(orderId, sessionId);

        StepVerifier.create(order)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Order not found for session: id=" + orderId));
                })
                .verify();

        verify(this.orderRepository, times(1)).findByIdAndSessionId(orderId, sessionId);
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty for getOrderByIdForSession")
    void getOrderByIdForSession_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        long orderId = 10L;
        String sessionId = "";

        Mono<Order> order = this.orderQueryService.getOrderByIdForSession(orderId, sessionId);

        StepVerifier.create(order)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.orderRepository);
    }

}
