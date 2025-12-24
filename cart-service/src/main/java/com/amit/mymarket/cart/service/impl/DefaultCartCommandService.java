package com.amit.mymarket.cart.service.impl;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.service.CartCommandService;
import com.amit.mymarket.cart.service.cache.CartCacheInvalidator;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.util.UserIdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DefaultCartCommandService implements CartCommandService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final CartCacheInvalidator cartCacheInvalidator;

    @Autowired
    public DefaultCartCommandService(CartRepository cartRepository, CartItemRepository cartItemRepository, CartCacheInvalidator cartCacheInvalidator) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartCacheInvalidator = cartCacheInvalidator;
    }

    @Override
    public Mono<Void> incrementCartItemQuantity(String userId, long itemId) {
        return UserIdUtils.ensureUserId(userId)
                .flatMap(this::getOrCreateActiveCart)
                .flatMap(cart -> this.cartItemRepository.incrementItemQuantity(cart.getId(), itemId))
                .then(this.cartCacheInvalidator.invalidateCart(userId));
    }

    @Override
    public Mono<Void> decrementCartItemQuantityOrDelete(String userId, long itemId) {
        return UserIdUtils.ensureUserId(userId)
                .flatMap(this::getRequiredActiveCart)
                .flatMap(cart ->
                        this.cartItemRepository.deleteWhenItemQuantityIsOne(cart.getId(), itemId)
                                .defaultIfEmpty(0)
                                .flatMap(deletedRowsCount -> {
                                    if (deletedRowsCount == 0) {
                                        return this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(cart.getId(), itemId);
                                    }
                                    return Mono.just(deletedRowsCount);
                                })
                )
                .then(this.cartCacheInvalidator.invalidateCart(userId));
    }

    @Override
    public Mono<Void> deleteCartItem(String userId, long itemId) {
        return UserIdUtils.ensureUserId(userId)
                .flatMap(this::getRequiredActiveCart)
                .flatMap(cart -> this.cartItemRepository.deleteCartItem(cart.getId(), itemId))
                .then(this.cartCacheInvalidator.invalidateCart(userId));
    }

    @Override
    public Mono<Void> clearActiveCart(String userId) {
        return UserIdUtils.ensureUserId(userId)
                .flatMap(sid ->
                        this.getRequiredActiveCart(sid)
                                .flatMap(cart -> this.cartItemRepository.deleteByCartId(cart.getId()))
                                .then(this.cartCacheInvalidator.invalidateCart(sid))
                );
    }

    private Mono<Cart> getRequiredActiveCart(String userId) {
        return this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Active cart not found for userId=" + userId)));
    }


    private Mono<Cart> getOrCreateActiveCart(String userId) {
        return this.cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Cart cart = new Cart();
                            cart.setUserId(userId);
                            cart.setStatus(CartStatus.ACTIVE);
                            return this.cartRepository.save(cart);
                        })
                );
    }

}
