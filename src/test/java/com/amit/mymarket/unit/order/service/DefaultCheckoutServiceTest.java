package com.amit.mymarket.unit.order.service;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.exception.ServiceException;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.repository.OrderItemRepository;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.service.impl.DefaultCheckoutService;
import com.amit.mymarket.order.service.util.OrderUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class DefaultCheckoutServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private DefaultCheckoutService checkoutService;

    @Test
    @DisplayName(value = "Should create order from active cart and clear cart when session identifier and cart and items are valid")
    void createOrderFromActiveCartAndClear_shouldCreateOrderAndClearCartWhenEverythingIsValid() {
        String sessionId = "session-123";
        long expectedOrderId = 42L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setSessionId(sessionId);
        activeCart.setStatus(CartStatus.ACTIVE);

        CartItemRow firstCartItemRow = new CartItemRow(1L, "Apple", "Green", "/a.png", 100L, 2);
        CartItemRow secondCartItemRow = new CartItemRow(2L, "Banana", "Yellow", "/b.png", 50L, 1);

        List<CartItemRow> cartItemList = List.of(firstCartItemRow, secondCartItemRow);
        long expectedTotalMinor = OrderUtils.calculateTotalMinor(cartItemList);

        Order savedOrder = new Order(sessionId, expectedTotalMinor);
        savedOrder.setId(expectedOrderId);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.findCartItems(sessionId)).thenReturn(Flux.fromIterable(cartItemList));
        when(this.orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(this.orderItemRepository.saveAll(anyList())).thenReturn(Flux.empty());
        when(this.cartItemRepository.deleteByCartId(activeCart.getId())).thenReturn(Mono.empty());
        when(this.cartRepository.save(any(Cart.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<Long> result = this.checkoutService.createOrderFromActiveCartAndClear(sessionId);

        StepVerifier.create(result)
                .expectNext(expectedOrderId)
                .verifyComplete();

        verify(this.cartRepository).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository).findCartItems(sessionId);
        verify(this.orderRepository).save(any(Order.class));
        verify(this.orderItemRepository).saveAll(anyList());
        verify(this.cartItemRepository).deleteByCartId(activeCart.getId());
        verify(this.cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty")
    void createOrderFromActiveCartAndClear_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = "   ";

        Mono<Long> orderId = this.checkoutService.createOrderFromActiveCartAndClear(sessionId);

        StepVerifier.create(orderId)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
        verifyNoInteractions(this.orderRepository);
        verifyNoInteractions(this.orderItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when active cart does not exist for session identifier")
    void createOrderFromActiveCartAndClear_shouldReturnErrorWhenActiveCartDoesNotExist() {
        String sessionId = "session-123";

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.empty());

        Mono<Long> orderId = this.checkoutService.createOrderFromActiveCartAndClear(sessionId);

        StepVerifier.create(orderId)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart not found for sessionId=" + sessionId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
        verifyNoInteractions(this.orderRepository);
        verifyNoInteractions(this.orderItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when active cart is empty for session identifier")
    void createOrderFromActiveCartAndClear_shouldReturnErrorWhenActiveCartIsEmpty() {
        String sessionId = "session-123";

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setSessionId(sessionId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));

        when(this.cartItemRepository.findCartItems(sessionId)).thenReturn(Flux.empty());

        Mono<Long> orderId = this.checkoutService.createOrderFromActiveCartAndClear(sessionId);

        StepVerifier.create(orderId)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart is empty for sessionId=" + sessionId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).findCartItems(sessionId);
        verifyNoInteractions(this.orderRepository);
        verifyNoInteractions(this.orderItemRepository);
    }
}
