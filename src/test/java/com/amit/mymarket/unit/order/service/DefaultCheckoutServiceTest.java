package com.amit.mymarket.unit.order.service;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.domain.entity.enums.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.item.domain.entity.Item;
import com.amit.mymarket.order.entity.Order;
import com.amit.mymarket.order.entity.enums.OrderStatus;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.service.exception.EmptyCartException;
import com.amit.mymarket.order.service.impl.DefaultCheckoutService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class DefaultCheckoutServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DefaultCheckoutService checkoutService;

    @Test
    @DisplayName(value = "Should throw EmptyCartException when no active cart exists")
    void createOrderFromActiveCartAndClear_shouldThrowWhenNoActiveCart() {
        String sessionId = "session-A";
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.checkoutService.createOrderFromActiveCartAndClear(sessionId))
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("active cart is empty");

        verifyNoInteractions(this.orderRepository, this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should throw EmptyCartException when active cart has no items")
    void createOrderFromActiveCartAndClear_shouldThrowWhenActiveCartEmpty() {
        String sessionId = "session-B";
        Cart emptyCart = mock(Cart.class);
        when(emptyCart.getItems()).thenReturn(Collections.emptySet());
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() -> this.checkoutService.createOrderFromActiveCartAndClear(sessionId))
                .isInstanceOf(EmptyCartException.class);

        verifyNoInteractions(this.orderRepository, this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should create order from active cart, save it, clear cart items and return new order id")
    void createOrderFromActiveCartAndClear_shouldCreateOrderSaveAndClearCart() {
        String sessionId = "session-C";

        Item item1 = mock(Item.class);
        when(item1.getId()).thenReturn(101L);
        when(item1.getPriceMinor()).thenReturn(1500L);

        CartItem cartItem1 = mock(CartItem.class);
        when(cartItem1.getItem()).thenReturn(item1);
        when(cartItem1.getQuantity()).thenReturn(2);

        Item item2 = mock(Item.class);
        when(item2.getId()).thenReturn(202L);
        when(item2.getPriceMinor()).thenReturn(999L);

        CartItem cartItem2 = mock(CartItem.class);
        when(cartItem2.getItem()).thenReturn(item2);
        when(cartItem2.getQuantity()).thenReturn(3);

        Cart cart = mock(Cart.class);
        when(cart.getId()).thenReturn(77L);
        when(cart.getItems()).thenReturn(new LinkedHashSet<>(Arrays.asList(cartItem1, cartItem2)));
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        Order persistedOrder = mock(Order.class);
        when(persistedOrder.getId()).thenReturn(555L);
        when(this.orderRepository.save(any(Order.class))).thenReturn(persistedOrder);

        long resultId = this.checkoutService.createOrderFromActiveCartAndClear(sessionId);

        assertThat(resultId).isEqualTo(555L);

        verify(this.orderRepository).save(argThat(order -> {
            assertThat(order.getSessionId()).isEqualTo(sessionId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getTotalMinor()).isEqualTo(3000L + 2997L);
            return true;
        }));

        verify(this.cartItemRepository).deleteCartItem(77L, 101L);
        verify(this.cartItemRepository).deleteCartItem(77L, 202L);

        verify(cart).setStatus(CartStatus.ORDERED);
        verify(this.cartRepository).save(cart);
    }

}
