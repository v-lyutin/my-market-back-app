package com.amit.mymarket.unit.cart.service;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.cart.service.impl.DefaultCartQueryService;
import com.amit.mymarket.common.exception.ServiceException;
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
class DefaultCartQueryServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private DefaultCartQueryService cartQueryService;

    @Test
    @DisplayName(value = "Should return cart items when session identifier is valid and cart exists")
    void getCartItems_shouldReturnCartItemsWhenSessionIdentifierIsValidAndCartExists() {
        String sessionId = "session-123";

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setSessionId(sessionId);
        cart.setStatus(CartStatus.ACTIVE);

        CartItemRow firstCartItemRow = new CartItemRow(1L, "Apple", "Green", "/a.png", 100L, 2);
        CartItemRow secondCartItemRow = new CartItemRow(2L, "Banana", "Yellow", "/b.png", 50L, 1);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.just(cart));

        when(this.cartItemRepository.findCartItems(sessionId)).thenReturn(Flux.just(firstCartItemRow, secondCartItemRow));

        Flux<CartItemRow> cartItemRows = this.cartQueryService.getCartItems(sessionId);

        StepVerifier.create(cartItemRows.collectList())
                .assertNext(cartItemRowList -> {
                    assertEquals(2, cartItemRowList.size());
                    assertEquals(1L, cartItemRowList.get(0).id());
                    assertEquals(2L, cartItemRowList.get(1).id());
                })
                .verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verify(this.cartItemRepository, times(1)).findCartItems(sessionId);
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty")
    void getCartItems_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = "   ";

        Flux<CartItemRow> cartItemRows = this.cartQueryService.getCartItems(sessionId);

        StepVerifier.create(cartItemRows)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartRepository);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return empty result when session identifier is valid but cart does not exist")
    void getCartItems_shouldReturnEmptyResultWhenCartDoesNotExist() {
        String sessionId = "session-123";

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)).thenReturn(Mono.empty());

        Flux<CartItemRow> cartItemRows = this.cartQueryService.getCartItems(sessionId);

        StepVerifier.create(cartItemRows).verifyComplete();

        verify(this.cartRepository, times(1)).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return total cart price when session identifier is valid")
    void calculateCartTotalPrice_shouldReturnTotalPriceWhenSessionIdentifierIsValid() {
        String sessionId = "session-123";

        when(this.cartItemRepository.calculateCartTotalPrice(sessionId)).thenReturn(Mono.just(500L));

        Mono<Long> totalPrice = this.cartQueryService.calculateCartTotalPrice(sessionId);

        StepVerifier.create(totalPrice)
                .expectNext(500L)
                .verifyComplete();

        verify(this.cartItemRepository, times(1)).calculateCartTotalPrice(sessionId);
    }

    @Test
    @DisplayName(value = "Should return zero when session identifier is valid but no price is returned")
    void calculateCartTotalPrice_shouldReturnZeroWhenRepositoryReturnsEmpty() {
        String sessionId = "session-123";

        when(this.cartItemRepository.calculateCartTotalPrice(sessionId)).thenReturn(Mono.empty());

        Mono<Long> totalPrice = this.cartQueryService.calculateCartTotalPrice(sessionId);

        StepVerifier.create(totalPrice)
                .expectNext(0L)
                .verifyComplete();

        verify(this.cartItemRepository, times(1)).calculateCartTotalPrice(sessionId);
    }

    @Test
    @DisplayName(value = "Should return error when session identifier is empty")
    void calculateCartTotalPrice_shouldReturnErrorWhenSessionIdentifierIsEmpty() {
        String sessionId = "";

        Mono<Long> totalPrice = this.cartQueryService.calculateCartTotalPrice(sessionId);

        StepVerifier.create(totalPrice)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ServiceException.class, throwable);
                    assertEquals("Session id is empty", throwable.getMessage());
                })
                .verify();

        verifyNoInteractions(this.cartItemRepository);
    }

}
