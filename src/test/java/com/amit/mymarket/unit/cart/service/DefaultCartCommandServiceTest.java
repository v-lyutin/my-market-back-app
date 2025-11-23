package com.amit.mymarket.unit.cart.service;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.impl.DefaultCartCommandService;
import com.amit.mymarket.item.entity.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class DefaultCartCommandServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private DefaultCartCommandService cartCommandService;

    @Test
    @DisplayName(value = "Should increment item quantity when active cart exists")
    void incrementCartItemQuantity_shouldIncrementQuantityWhenActiveCartExists() {
        String sessionId = "session-1";
        long itemId = 100L;

        Cart existingCart = mock(Cart.class);
        when(existingCart.getId()).thenReturn(1L);
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(existingCart));

        this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);

        verify(this.cartItemRepository).incrementItemQuantity(1L, itemId);
        verify(this.cartRepository, never()).save(any());
    }

    @Test
    @DisplayName(value = "Should create active cart and then increment item quantity when none exists")
    void incrementCartItemQuantity_shouldCreateActiveCartAndIncrementWhenMissing() {
        String sessionId = "session-2";
        long itemId = 200L;

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        Cart savedCart = mock(Cart.class);
        when(savedCart.getId()).thenReturn(42L);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);

        verify(this.cartRepository).save(any(Cart.class));
        verify(this.cartItemRepository).incrementItemQuantity(42L, itemId);
    }

    @Test
    @DisplayName(value = "Should do nothing on decrement when no active cart")
    void decrementCartItemQuantityOrDelete_shouldDoNothingOnDecrementWhenNoActiveCart() {
        String sessionId = "session-3";
        long itemId = 300L;

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);

        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should delete cart item when quantity is one")
    void decrementCartItemQuantityOrDelete_shouldDeleteWhenQuantityIsOne() {
        String sessionId = "session-4";
        long itemId = 400L;

        Cart cart = mock(Cart.class);
        when(cart.getId()).thenReturn(7L);
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(this.cartItemRepository.deleteWhenItemQuantityIsOne(7L, itemId)).thenReturn(1);

        this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);

        verify(this.cartItemRepository).deleteWhenItemQuantityIsOne(7L, itemId);
        verify(this.cartItemRepository, never()).decrementWhenItemQuantityGreaterThanOne(anyLong(), anyLong());
    }

    @Test
    @DisplayName(value = "Should decrement when quantity is greater than one")
    void decrementCartItemQuantityOrDelete_shouldDecrementWhenQuantityGreaterThanOne() {
        String sessionId = "session-5";
        long itemId = 500L;

        Cart cart = mock(Cart.class);
        when(cart.getId()).thenReturn(9L);
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(this.cartItemRepository.deleteWhenItemQuantityIsOne(9L, itemId)).thenReturn(0);

        this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);

        verify(this.cartItemRepository).deleteWhenItemQuantityIsOne(9L, itemId);
        verify(this.cartItemRepository).decrementWhenItemQuantityGreaterThanOne(9L, itemId);
    }

    @Test
    @DisplayName(value = "Should do nothing on delete when no active cart")
    void deleteCartItem_shouldDoNothingOnDeleteWhenNoActiveCart() {
        String sessionId = "session-6";
        long itemId = 600L;

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        this.cartCommandService.deleteCartItem(sessionId, itemId);

        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should delete cart item when active cart exists")
    void deleteCartItem_shouldDeleteCartItemWhenActiveCartExists() {
        String sessionId = "session-7";
        long itemId = 700L;

        Cart cart = mock(Cart.class);
        when(cart.getId()).thenReturn(11L);
        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        this.cartCommandService.deleteCartItem(sessionId, itemId);

        verify(this.cartItemRepository).deleteCartItem(11L, itemId);
    }

    @Test
    @DisplayName(value = "Should do nothing on clear when no active cart")
    void clearActiveCart_shouldDoNothingOnClearWhenNoActiveCart() {
        String sessionId = "session-8";
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        this.cartCommandService.clearActiveCart(sessionId);

        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should delete all items of active cart during clear")
    void clearActiveCart_shouldDeleteAllItemsOnClear() {
        String sessionId = "session-9";

        Item item1 = mock(Item.class);
        when(item1.getId()).thenReturn(101L);
        CartItem cartItem1 = mock(CartItem.class);
        when(cartItem1.getItemId()).thenReturn(item1);

        Item item2 = mock(Item.class);
        when(item2.getId()).thenReturn(202L);
        CartItem cartItem2 = mock(CartItem.class);
        when(cartItem2.getItemId()).thenReturn(item2);

        Cart cart = mock(Cart.class);
        when(cart.getId()).thenReturn(13L);
        when(cart.getItems()).thenReturn(new LinkedHashSet<>(Arrays.asList(cartItem1, cartItem2)));
        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        this.cartCommandService.clearActiveCart(sessionId);

        verify(this.cartItemRepository).deleteCartItem(13L, 101L);
        verify(this.cartItemRepository).deleteCartItem(13L, 202L);
    }

}
