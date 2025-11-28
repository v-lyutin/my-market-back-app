package com.amit.mymarket.unit.cart.service;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.impl.DefaultCartCommandService;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName(value = "Should increment cart item quantity when active cart exists for session identifier")
    void incrementCartItemQuantity_shouldIncrementWhenActiveCartExists() {
        String sessionId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setSessionId(sessionId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.incrementItemQuantity(activeCart.getId(), itemId)).thenReturn(Mono.just(1));

        Mono<Void> result = this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).incrementItemQuantity(eq(5L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should create active cart and increment item quantity when no active cart exists")
    void incrementCartItemQuantity_shouldCreateCartWhenNoActiveCartExists() {
        String sessionId = "session-123";
        long itemId = 10L;

        Cart createdCart = new Cart();
        createdCart.setId(7L);
        createdCart.setSessionId(sessionId);
        createdCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.empty());
        when(this.cartRepository.save(any(Cart.class))).thenReturn(Mono.just(createdCart));
        when(this.cartItemRepository.incrementItemQuantity(createdCart.getId(), itemId)).thenReturn(Mono.just(1));

        Mono<Void> result = this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartRepository, times(1)).save(any(Cart.class));
        verify(this.cartItemRepository, times(1)).incrementItemQuantity(eq(7L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty on increment")
    void incrementCartItemQuantity_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = "   ";
        long itemId = 10L;

        Mono<Void> result = this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should delete cart item when quantity is one and not decrement further")
    void decrementCartItemQuantityOrDelete_shouldDeleteWhenQuantityIsOne() {
        String sessionId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setSessionId(sessionId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(cartItemRepository.deleteWhenItemQuantityIsOne(activeCart.getId(), itemId)).thenReturn(Mono.just(1));

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteWhenItemQuantityIsOne(eq(5L), eq(10L));
        verify(this.cartItemRepository, never()).decrementWhenItemQuantityGreaterThanOne(anyLong(), anyLong());
    }

    @Test
    @DisplayName(value = "Should decrement cart item quantity when quantity is greater than one")
    void decrementCartItemQuantityOrDelete_shouldDecrementWhenQuantityIsGreaterThanOne() {
        String sessionId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setSessionId(sessionId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.deleteWhenItemQuantityIsOne(activeCart.getId(), itemId)).thenReturn(Mono.just(0));
        when(this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(activeCart.getId(), itemId)).thenReturn(Mono.just(1));

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteWhenItemQuantityIsOne(eq(5L), eq(10L));
        verify(this.cartItemRepository, times(1)).decrementWhenItemQuantityGreaterThanOne(eq(5L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty on decrement")
    void decrementCartItemQuantityOrDelete_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = "";
        long itemId = 10L;

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when active cart does not exist on decrement")
    void decrementCartItemQuantityOrDelete_shouldReturnErrorWhenActiveCartDoesNotExist() {
        String sessionId = "session-123";
        long itemId = 10L;

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart not found for sessionId=" + sessionId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should delete cart item when active cart exists")
    void deleteCartItem_shouldDeleteCartItemWhenActiveCartExists() {
        String sessionId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setSessionId(sessionId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.deleteCartItem(activeCart.getId(), itemId)).thenReturn(Mono.just(1));

        Mono<Void> result = this.cartCommandService.deleteCartItem(sessionId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteCartItem(eq(5L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should return error when active cart does not exist on delete cart item")
    void deleteCartItem_shouldReturnErrorWhenActiveCartDoesNotExist() {
        String sessionId = "session-123";
        long itemId = 10L;

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.deleteCartItem(sessionId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart not found for sessionId=" + sessionId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty on delete cart item")
    void deleteCartItem_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = "  ";
        long itemId = 10L;

        Mono<Void> result = this.cartCommandService.deleteCartItem(sessionId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should clear active cart when active cart exists")
    void clearActiveCart_shouldClearActiveCartWhenActiveCartExists() {
        String sessionId = "session-123";

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setSessionId(sessionId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.deleteByCartId(activeCart.getId())).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.clearActiveCart(sessionId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteByCartId(eq(5L));
    }

    @Test
    @DisplayName(value = "Should return error when active cart does not exist on clear")
    void clearActiveCart_shouldReturnErrorWhenActiveCartDoesNotExist() {
        String sessionId = "session-123";

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.clearActiveCart(sessionId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart not found for sessionId=" + sessionId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty on clear")
    void clearActiveCart_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = null;

        Mono<Void> result = this.cartCommandService.clearActiveCart(sessionId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

}
