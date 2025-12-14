package com.amit.mymarket.cart.service.cache;

import reactor.core.publisher.Mono;

public interface CartCacheInvalidator {

    Mono<Void> invalidateCart(String sessionId);

}
