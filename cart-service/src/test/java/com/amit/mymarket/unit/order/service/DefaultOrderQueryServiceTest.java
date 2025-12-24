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
    @DisplayName(value = "Should return orders for given user identifier")
    void getOrdersByUserId_shouldReturnOrdersForGivenUserIdIdentifier() {
        String userId = "session-123";

        Order firstOrder = new Order();
        firstOrder.setId(1L);
        firstOrder.setUserId(userId);

        Order secondOrder = new Order();
        secondOrder.setId(2L);
        secondOrder.setUserId(userId);

        when(this.orderRepository.findAllByUserId(userId)).thenReturn(Flux.just(firstOrder, secondOrder));

        Flux<Order> orders = this.orderQueryService.getOrdersByUserId(userId);

        StepVerifier.create(orders.collectList())
                .assertNext(orderList -> {
                    assertEquals(2, orderList.size());
                    assertEquals(1L, orderList.get(0).getId());
                    assertEquals(2L, orderList.get(1).getId());
                })
                .verifyComplete();

        verify(this.orderRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName(value = "Should return error when user identifier is empty for getOrdersByUserId")
    void getOrdersById_shouldReturnErrorWhenUserIdIdentifierIsEmpty() {
        String userId = "   ";

        Flux<Order> orders = this.orderQueryService.getOrdersByUserId(userId);

        StepVerifier.create(orders)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("userId is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.orderRepository);
    }

    @Test
    @DisplayName(value = "Should return order when order exists for given identifier and user identifier")
    void getOrderByIdForUserId_shouldReturnOrderWhenExistsForUserId() {
        long orderId = 10L;
        String userId = "session-123";

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        when(this.orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Mono.just(order));

        Mono<Order> result = this.orderQueryService.getOrderByIdForUserId(orderId, userId);

        StepVerifier.create(result)
                .assertNext(foundOrder -> {
                    assertEquals(orderId, foundOrder.getId());
                    assertEquals(userId, foundOrder.getUserId());
                })
                .verifyComplete();

        verify(this.orderRepository, times(1)).findByIdAndUserId(orderId, userId);
    }

    @Test
    @DisplayName(value = "Should throw ResourceNotFoundException when order does not exist for user identifier")
    void getOrderByIdForUserId_shouldThrowResourceNotFoundExceptionWhenOrderDoesNotExistForUserId() {
        long orderId = 10L;
        String userId = "session-123";

        when(this.orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Mono.empty());

        Mono<Order> order = this.orderQueryService.getOrderByIdForUserId(orderId, userId);

        StepVerifier.create(order)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Order not found for user: id=" + orderId));
                })
                .verify();

        verify(this.orderRepository, times(1)).findByIdAndUserId(orderId, userId);
    }

    @Test
    @DisplayName(value = "Should return error when user identifier is empty for getOrderByIdForUserId")
    void getOrderByIdForUserId_shouldReturnErrorWhenUserIdIdentifierIsEmpty() {
        long orderId = 10L;
        String userId = "";

        Mono<Order> order = this.orderQueryService.getOrderByIdForUserId(orderId, userId);

        StepVerifier.create(order)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("userId is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.orderRepository);
    }

}
