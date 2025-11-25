package com.amit.mymarket.unit.cart.service;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.impl.DefaultCartQueryService;
import com.amit.mymarket.item.entity.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName(value = "Should return an empty list when no active cart is found")
    void findBySessionIdAndStatus_shouldReturnEmptyListWhenCartNotFound() {
        String sessionId = "session-1";
        when(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        List<CartItem> result = this.cartQueryService.fetchCartItems(sessionId);

        assertThat(result).isEmpty();
        verify(this.cartRepository).findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should sort items by title (case-insensitive) and then by ID")
    void findBySessionIdAndStatus_shouldSortItemsByTitleCaseInsensitiveThenById() {
        String sessionId = "session-2";

        Item item1 = mock(Item.class);
        when(item1.getTitle()).thenReturn("alpha");
        when(item1.getId()).thenReturn(20L);

        CartItem cartItem1 = mock(CartItem.class);
        when(cartItem1.getItemId()).thenReturn(item1);

        Item item2 = mock(Item.class);
        when(item2.getTitle()).thenReturn("Alpha");
        when(item2.getId()).thenReturn(10L);

        CartItem cartItem2 = mock(CartItem.class);
        when(cartItem2.getItemId()).thenReturn(item2);

        Item item3 = mock(Item.class);
        when(item3.getTitle()).thenReturn("beta");

        CartItem cartItem3 = mock(CartItem.class);
        when(cartItem3.getItemId()).thenReturn(item3);

        Set<CartItem> items = new HashSet<>(Arrays.asList(cartItem1, cartItem2, cartItem3));
        Cart cart = mock(Cart.class);
        when(cart.getItems()).thenReturn(items);

        when(this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        List<CartItem> result = this.cartQueryService.fetchCartItems(sessionId);

        assertThat(result).containsExactly(cartItem2, cartItem1, cartItem3);
    }

    @Test
    @DisplayName(value = "Should return 0 when repository returns null for cart total")
    void calculateCartTotal_shouldReturnZeroWhenRepositoryReturnsNull() {
        String sessionId = "session-3";
        when(this.cartItemRepository.calculateCartTotalPrice(sessionId)).thenReturn(null);

        long cartTotalMinor = this.cartQueryService.calculateCartTotalMinor(sessionId);

        assertThat(cartTotalMinor).isZero();
        verify(this.cartItemRepository).calculateCartTotalPrice(sessionId);
    }

    @Test
    @DisplayName(value = "Should return the repository value when it is not null")
    void calculateCartTotalMinor_shouldReturnValueWhenRepositoryReturnsNonNull() {
        String sessionId = "session-4";
        when(this.cartItemRepository.calculateCartTotalPrice(sessionId)).thenReturn(12345L);

        long cartTotalMinor = this.cartQueryService.calculateCartTotalMinor(sessionId);

        assertThat(cartTotalMinor).isEqualTo(12345L);
    }

}
