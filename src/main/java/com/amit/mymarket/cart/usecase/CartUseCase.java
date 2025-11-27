package com.amit.mymarket.cart.usecase;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.cart.api.type.CartAction;
import reactor.core.publisher.Mono;

public interface CartUseCase {

    Mono<CartViewDto> getCart(String sessionId);

    Mono<Void> mutateCartItem(String sessionId, long itemId, CartAction cartAction);

}
