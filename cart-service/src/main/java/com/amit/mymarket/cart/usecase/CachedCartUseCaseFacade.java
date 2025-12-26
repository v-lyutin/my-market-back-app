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
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class CachedCartUseCaseFacade implements CartUseCase {

    private static final Duration CART_TTL = Duration.ofMinutes(5);

    private final CartQueryService cartQueryService;

    private final CartCommandService cartCommandService;

    private final ItemMapper itemMapper;

    private final CartMapper cartMapper;

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CachedCartUseCaseFacade(CartQueryService cartQueryService,
                                   CartCommandService cartCommandService,
                                   ItemMapper itemMapper,
                                   CartMapper cartMapper,
                                   ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.cartQueryService = cartQueryService;
        this.cartCommandService = cartCommandService;
        this.itemMapper = itemMapper;
        this.cartMapper = cartMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CartViewDto> getCart(String userId) {
        String cacheKey = buildCartKey(userId);

        return this.redisTemplate.opsForValue()
                .get(cacheKey)
                .cast(CartViewDto.class)
                .switchIfEmpty(Mono.defer(() -> {
                    Mono<List<CartItemRow>> cartItemRows = this.cartQueryService.getCartItems(userId).collectList();
                    Mono<Long> cartTotalPrice = this.cartQueryService.calculateCartTotalPrice(userId);
                    return Mono.zip(cartItemRows, cartTotalPrice)
                            .map(tuple -> {
                                List<CartItemRow> cartRows = tuple.getT1();
                                Long totalMinor = tuple.getT2();

                                List<ItemInfoView> mappedCartItemViews = cartRows.stream()
                                        .map(this.itemMapper::toItemInfoView)
                                        .toList();

                                return this.cartMapper.toCartViewDto(mappedCartItemViews, totalMinor);
                            })
                            .flatMap(cartViewDto ->
                                    this.redisTemplate.opsForValue()
                                            .set(cacheKey, cartViewDto, CART_TTL)
                                            .thenReturn(cartViewDto)
                            );
                }));
    }

    @Override
    @Transactional
    public Mono<Void> mutateCartItem(String userId, long itemId, CartAction cartAction) {
        String cacheKey = buildCartKey(userId);

        Mono<Void> command = switch (cartAction) {
            case PLUS -> this.cartCommandService.incrementCartItemQuantity(userId, itemId);
            case MINUS -> this.cartCommandService.decrementCartItemQuantityOrDelete(userId, itemId);
            case DELETE -> this.cartCommandService.deleteCartItem(userId, itemId);
        };

        return command
                .then(this.redisTemplate.opsForValue()
                        .delete(cacheKey)
                        .then()
                );
    }

    private String buildCartKey(String userId) {
        return "cart:view:" + userId;
    }

}
