package com.amit.mymarket.cart.usecase;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.api.type.CartAction;
import com.amit.mymarket.cart.service.CartCommandService;
import com.amit.mymarket.cart.service.CartQueryService;
import com.amit.mymarket.cart.api.mapper.CartMapper;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.mapper.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DefaultCartUseCaseFacade implements CartUseCase {

    private final CartQueryService cartQueryService;

    private final CartCommandService cartCommandService;

    private final ItemMapper itemMapper;

    private final CartMapper cartMapper;

    @Autowired
    public DefaultCartUseCaseFacade(CartQueryService cartQueryService,
                                    CartCommandService cartCommandService,
                                    ItemMapper itemMapper,
                                    CartMapper cartMapper) {
        this.cartQueryService = cartQueryService;
        this.cartCommandService = cartCommandService;
        this.itemMapper = itemMapper;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CartViewDto getCart(String sessionId) {
        List<CartItem> cartItems = this.cartQueryService.fetchCartItems(sessionId);

        List<ItemInfoView> items = cartItems.stream()
                .map(cartItem -> this.itemMapper.toItemInfoView(cartItem.getItem(), cartItem.getQuantity() ))
                .toList();

        Long totalMinor = this.cartQueryService.calculateCartTotalMinor(sessionId);
        return this.cartMapper.toCartViewDto(items, totalMinor);
    }

    @Override
    @Transactional
    public void mutateCartItem(String sessionId, long itemId, CartAction cartAction) {
        switch (cartAction) {
            case PLUS -> this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);
            case MINUS -> this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);
            case DELETE -> this.cartCommandService.deleteCartItem(sessionId, itemId);
        }
    }

}
