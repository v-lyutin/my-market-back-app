package com.amit.mymarket.cart.service.impl;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.CartCommandService;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
    public Mono<Void> incrementCartItemQuantity(String sessionId, long itemId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(this::getOrCreateActiveCart)
                .flatMap(cart -> this.cartItemRepository.incrementItemQuantity(cart.getId(), itemId))
                .then();
    }

    @Override
    public Mono<Void> decrementCartItemQuantityOrDelete(String sessionId, long itemId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(this::getRequiredActiveCart)
                .flatMap(cart ->
                        this.cartItemRepository.deleteWhenItemQuantityIsOne(cart.getId(), itemId)
                                .flatMap(deletedRowsCount -> {
                                    if (deletedRowsCount == 0) {
                                        return this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(cart.getId(), itemId);
                                    }
                                    return Mono.just(deletedRowsCount);
                                })
                )
                .then();
    }

    @Override
    public Mono<Void> deleteCartItem(String sessionId, long itemId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(this::getRequiredActiveCart)
                .flatMap(cart -> this.cartItemRepository.deleteCartItem(cart.getId(), itemId))
                .then();
    }

    @Override
    public Mono<Void> clearActiveCart(String sessionId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(this::getRequiredActiveCart)
                .flatMap(cart -> this.cartItemRepository.deleteByCartId(cart.getId()))
                .then();
    }

    private Mono<Cart> getRequiredActiveCart(String sessionId) {
        return this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Active cart not found for sessionId=" + sessionId)));
    }


    private Mono<Cart> getOrCreateActiveCart(String sessionId) {
        return this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Cart cart = new Cart();
                            cart.setSessionId(sessionId);
                            cart.setStatus(CartStatus.ACTIVE);
                            return this.cartRepository.save(cart);
                        })
                );
    }

}
