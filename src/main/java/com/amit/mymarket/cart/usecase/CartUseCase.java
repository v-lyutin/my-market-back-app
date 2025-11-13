package com.amit.mymarket.cart.usecase;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.cart.domain.type.CartAction;

public interface CartUseCase {

    CartViewDto getCart(String sessionId);

    void mutateCartItem(String sessionId, long itemId, CartAction cartAction);

}
