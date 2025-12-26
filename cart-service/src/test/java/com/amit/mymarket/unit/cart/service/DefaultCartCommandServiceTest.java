package com.amit.mymarket.unit.cart.service;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.cache.CartCacheInvalidator;
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

    @Mock
    private CartCacheInvalidator cartCacheInvalidator;

    @InjectMocks
    private DefaultCartCommandService cartCommandService;

    @Test
    @DisplayName(value = "Should increment cart item quantity when active cart exists for user identifier")
    void incrementCartItemQuantity_shouldIncrementWhenActiveCartExists() {
        String userId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setUserId(userId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.incrementItemQuantity(activeCart.getId(), itemId)).thenReturn(Mono.just(1));
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.incrementCartItemQuantity(userId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).incrementItemQuantity(eq(5L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should create active cart and increment item quantity when no active cart exists")
    void incrementCartItemQuantity_shouldCreateCartWhenNoActiveCartExists() {
        String userId = "session-123";
        long itemId = 10L;

        Cart createdCart = new Cart();
        createdCart.setId(7L);
        createdCart.setUserId(userId);
        createdCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.empty());
        when(this.cartRepository.save(any(Cart.class))).thenReturn(Mono.just(createdCart));
        when(this.cartItemRepository.incrementItemQuantity(createdCart.getId(), itemId)).thenReturn(Mono.just(1));
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.incrementCartItemQuantity(userId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verify(this.cartRepository, times(1)).save(any(Cart.class));
        verify(this.cartItemRepository, times(1)).incrementItemQuantity(eq(7L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should return error when user identifier is empty on increment")
    void incrementCartItemQuantity_shouldReturnErrorWhenUserIdIsEmpty() {
        String userId = "   ";
        long itemId = 10L;

        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.incrementCartItemQuantity(userId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("userId is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should delete cart item when quantity is one and not decrement further")
    void decrementCartItemQuantityOrDelete_shouldDeleteWhenQuantityIsOne() {
        String userId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setUserId(userId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(cartItemRepository.deleteWhenItemQuantityIsOne(activeCart.getId(), itemId)).thenReturn(Mono.just(1));
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(userId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteWhenItemQuantityIsOne(eq(5L), eq(10L));
        verify(this.cartItemRepository, never()).decrementWhenItemQuantityGreaterThanOne(anyLong(), anyLong());
    }

    @Test
    @DisplayName(value = "Should decrement cart item quantity when quantity is greater than one")
    void decrementCartItemQuantityOrDelete_shouldDecrementWhenQuantityIsGreaterThanOne() {
        String userId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setUserId(userId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.deleteWhenItemQuantityIsOne(activeCart.getId(), itemId)).thenReturn(Mono.just(0));
        when(this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(activeCart.getId(), itemId)).thenReturn(Mono.just(1));
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(userId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteWhenItemQuantityIsOne(eq(5L), eq(10L));
        verify(this.cartItemRepository, times(1)).decrementWhenItemQuantityGreaterThanOne(eq(5L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should return error when user identifier is empty on decrement")
    void decrementCartItemQuantityOrDelete_shouldReturnErrorWhenUserIdIsEmpty() {
        String userId = "";
        long itemId = 10L;

        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(userId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("userId is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when active cart does not exist on decrement")
    void decrementCartItemQuantityOrDelete_shouldReturnErrorWhenActiveCartDoesNotExist() {
        String userId = "session-123";
        long itemId = 10L;

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.empty());
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.decrementCartItemQuantityOrDelete(userId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart not found for userId=" + userId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should delete cart item when active cart exists")
    void deleteCartItem_shouldDeleteCartItemWhenActiveCartExists() {
        String userId = "session-123";
        long itemId = 10L;

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setUserId(userId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.deleteCartItem(activeCart.getId(), itemId)).thenReturn(Mono.just(1));
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.deleteCartItem(userId, itemId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteCartItem(eq(5L), eq(10L));
    }

    @Test
    @DisplayName(value = "Should return error when active cart does not exist on delete cart item")
    void deleteCartItem_shouldReturnErrorWhenActiveCartDoesNotExist() {
        String userId = "session-123";
        long itemId = 10L;

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.empty());
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.deleteCartItem(userId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart not found for userId=" + userId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when user identifier is empty on delete cart item")
    void deleteCartItem_shouldReturnErrorWhenUserIdIsEmpty() {
        String userId = "  ";
        long itemId = 10L;

        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.deleteCartItem(userId, itemId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("userId is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should clear active cart when active cart exists")
    void clearActiveCart_shouldClearActiveCartWhenActiveCartExists() {
        String userId = "session-123";

        Cart activeCart = new Cart();
        activeCart.setId(5L);
        activeCart.setUserId(userId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.just(activeCart));
        when(this.cartItemRepository.deleteByCartId(activeCart.getId())).thenReturn(Mono.empty());
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.clearActiveCart(userId);

        StepVerifier.create(result).verifyComplete();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).deleteByCartId(eq(5L));
    }

    @Test
    @DisplayName(value = "Should return error when active cart does not exist on clear")
    void clearActiveCart_shouldReturnErrorWhenActiveCartDoesNotExist() {
        String userId = "session-123";

        when(this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Mono.empty());
        when(this.cartCacheInvalidator.invalidateCart(userId)).thenReturn(Mono.empty());

        Mono<Void> result = this.cartCommandService.clearActiveCart(userId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Active cart not found for userId=" + userId));
                })
                .verify();

        verify(this.cartRepository, times(1)).findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return error when user identifier is empty on clear")
    void clearActiveCart_shouldReturnErrorWhenUserIdIsEmpty() {
        String userId = null;

        Mono<Void> result = this.cartCommandService.clearActiveCart(userId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("userId is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

}
