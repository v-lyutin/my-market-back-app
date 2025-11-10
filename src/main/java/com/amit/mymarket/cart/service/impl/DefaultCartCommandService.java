package com.amit.mymarket.cart.service.impl;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.entity.enums.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.CartCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultCartCommandService implements CartCommandService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    @Autowired
    public DefaultCartCommandService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public void incrementCartItemQuantity(String sessionId, long itemId) {
        Cart cart = this.getOrCreateActiveCart(sessionId);
        this.cartItemRepository.incrementItemQuantity(cart.getId(), itemId);
    }

    @Override
    public void decrementCartItemQuantityOrDelete(String sessionId, long itemId) {
        Cart cart = this.getActiveCartOrNull(sessionId);
        if (cart == null) {
            return;
        }
        int deletedRowsCount = this.cartItemRepository.deleteWhenItemQuantityIsOne(cart.getId(), itemId);
        if (deletedRowsCount == 0) {
            this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(cart.getId(), itemId);
        }
    }

    // TODO: create bulk query
    @Override
    public void deleteCartItem(String sessionId, long itemId) {
        Cart cart = this.getActiveCartOrNull(sessionId);
        if (cart == null) {
            return;
        }
        this.cartItemRepository.deleteCartItem(cart.getId(), itemId);
    }

    @Override
    public void clearActiveCart(String sessionId) {
        Cart cart = this.getActiveCartOrNull(sessionId);
        if (cart == null) {
            return;
        }
        cart.getItems().forEach(cartItem ->
                this.cartItemRepository.deleteCartItem(cart.getId(), cartItem.getItem().getId())
        );
    }

    /**
     * Returns ACTIVE cart or creates a new one if missing.
     */
    private Cart getOrCreateActiveCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setSessionId(sessionId);
                    cart.setStatus(CartStatus.ACTIVE);
                    return this.cartRepository.save(cart);
                });
    }

    /**
     * Returns ACTIVE cart or null if not found.
     */
    private Cart getActiveCartOrNull(String sessionId) {
        return this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).orElse(null);
    }

}
