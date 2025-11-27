package com.amit.mymarket.cart.usecase;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.cart.api.mapper.CartMapper;
import com.amit.mymarket.cart.api.type.CartAction;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.cart.service.CartCommandService;
import com.amit.mymarket.cart.service.CartQueryService;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.mapper.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
    public Mono<CartViewDto> getCart(String sessionId) {
        Mono<List<CartItemRow>> cartItemRows = this.cartQueryService.getCartItems(sessionId).collectList();

        Mono<Long> cartTotalPrice = this.cartQueryService.calculateCartTotalPrice(sessionId);

        return Mono.zip(cartItemRows, cartTotalPrice)
                .map(tuple -> {
                    List<CartItemRow> cartRows = tuple.getT1();
                    Long totalMinor = tuple.getT2();

                    List<ItemInfoView> mappedCartItemViews = cartRows.stream()
                            .map(this.itemMapper::toItemInfoView)
                            .toList();

                    return this.cartMapper.toCartViewDto(mappedCartItemViews, totalMinor);
                });
    }

    @Override
    @Transactional
    public Mono<Void> mutateCartItem(String sessionId, long itemId, CartAction cartAction) {
        return switch (cartAction) {
            case PLUS -> this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);
            case MINUS -> this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);
            case DELETE -> this.cartCommandService.deleteCartItem(sessionId, itemId);
        };
    }

}
