package com.amit.mymarket.order.service.impl;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.util.SessionUtils;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;
import com.amit.mymarket.order.repository.OrderItemRepository;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.service.CheckoutService;
import com.amit.mymarket.order.service.util.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DefaultCheckoutService implements CheckoutService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public DefaultCheckoutService(CartRepository cartRepository, CartItemRepository cartItemRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public Mono<Long> createOrderFromActiveCartAndClear(String sessionId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(this::getRequiredActiveCart)
                .zipWhen(cart -> this.getCartItems(cart.getSessionId()))
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    List<CartItemRow> cartRows = tuple.getT2();
                    return this.createOrderAndClearCart(cart, cartRows);
                });
    }

    private Mono<Cart> getRequiredActiveCart(String sessionId) {
        return this.cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Active cart not found for sessionId=" + sessionId)));
    }

    private Mono<List<CartItemRow>> getCartItems(String sessionId) {
        return this.cartItemRepository.findCartItems(sessionId)
                .collectList()
                .flatMap(cartItemRows -> {
                    if (CollectionUtils.isEmpty(cartItemRows)) {
                        return Mono.error(new ResourceNotFoundException("Active cart is empty for sessionId=" + sessionId));
                    }
                    return Mono.just(cartItemRows);
                });
    }

    private Mono<Long> createOrderAndClearCart(Cart cart, List<CartItemRow> cartRows) {
        long totalMinor = OrderUtils.calculateTotalMinor(cartRows);

        Order order = new Order(cart.getSessionId(), totalMinor);

        return this.orderRepository.save(order)
                .flatMap(savedOrder -> {
                    List<OrderItem> orderItems = OrderUtils.buildOrderItems(savedOrder, cartRows);

                    return this.orderItemRepository.saveAll(orderItems)
                            .then(this.clearCart(cart))
                            .thenReturn(savedOrder.getId());
                });
    }

    private Mono<Void> clearCart(Cart cart) {
        return this.cartItemRepository.deleteByCartId(cart.getId())
                .then(Mono.defer(() -> {
                    cart.setStatus(CartStatus.ORDERED);
                    return this.cartRepository.save(cart).then();
                }));
    }

}
