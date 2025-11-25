package com.amit.mymarket.cart.service.impl;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.CartQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultCartQueryService implements CartQueryService {

    private static final Comparator<CartItem> SORT_BY_TITLE = Comparator.comparing(
                    cartItem -> cartItem.getItemId().getTitle(),
                    String.CASE_INSENSITIVE_ORDER
            );

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    @Autowired
    public DefaultCartQueryService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public List<CartItem> fetchCartItems(String sessionId) {
        return this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .map(Cart::getItems)
                .stream()
                .flatMap(Collection::stream)
                .sorted(SORT_BY_TITLE.thenComparing(cartItem -> cartItem.getItemId().getId()))
                .toList();
    }

    @Override
    public long calculateCartTotalMinor(String sessionId) {
        Long cartTotalMinor = this.cartItemRepository.calculateCartTotalPrice(sessionId);
        return cartTotalMinor != null ? cartTotalMinor : 0L;
    }

}
