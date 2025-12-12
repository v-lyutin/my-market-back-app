package com.amit.mymarket.cart.service.impl;

import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.cart.service.CartQueryService;
import com.amit.mymarket.common.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DefaultCartQueryService implements CartQueryService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    @Autowired
    public DefaultCartQueryService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Flux<CartItemRow> getCartItems(String sessionId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(id -> this.cartRepository.findBySessionIdAndStatus(id, CartStatus.ACTIVE))
                .flatMapMany(cart -> this.cartItemRepository.findCartItems(sessionId));
    }

    @Override
    public Mono<Long> calculateCartTotalPrice(String sessionId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(this.cartItemRepository::calculateCartTotalPrice)
                .defaultIfEmpty(0L);
    }

}
