package com.amit.mymarket.order.service.impl;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.cart.service.CartQueryService;
import com.amit.mymarket.cart.service.cache.CartCacheInvalidator;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.exception.ServiceException;
import com.amit.mymarket.common.util.SessionUtils;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;
import com.amit.mymarket.order.repository.OrderItemRepository;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.service.CheckoutService;
import com.amit.mymarket.order.service.exception.InsufficientFundsException;
import com.amit.mymarket.order.service.model.CheckoutAvailability;
import com.amit.mymarket.order.service.util.OrderUtils;
import com.amit.payment.client.exception.PaymentServiceUnavailableException;
import com.amit.payment.client.service.PaymentServiceGateway;
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

    private final PaymentServiceGateway paymentServiceGateway;

    private final CartQueryService cartQueryService;

    private final CartCacheInvalidator cartCacheInvalidator;

    @Autowired
    public DefaultCheckoutService(CartRepository cartRepository,
                                  CartItemRepository cartItemRepository,
                                  OrderRepository orderRepository,
                                  OrderItemRepository orderItemRepository,
                                  PaymentServiceGateway paymentServiceGateway,
                                  CartQueryService cartQueryService,
                                  CartCacheInvalidator cartCacheInvalidator) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentServiceGateway = paymentServiceGateway;
        this.cartQueryService = cartQueryService;
        this.cartCacheInvalidator = cartCacheInvalidator;
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

    @Override
    public Mono<CheckoutAvailability> getCheckoutAvailability(String sessionId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(sid -> this.cartQueryService.calculateCartTotalPrice(sid)
                        .flatMap(totalMinor -> this.paymentServiceGateway.getBalanceKopecks(sid)
                                .map(balance -> {
                                    if (balance >= totalMinor) {
                                        return new CheckoutAvailability(true, null);
                                    }
                                    return new CheckoutAvailability(false, "Insufficient funds");
                                })
                                .onErrorResume(
                                        PaymentServiceUnavailableException.class,
                                        exception -> Mono.just(new CheckoutAvailability(false, "Payment service is unavailable"))
                                )
                        )
                );
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

        return this.paymentServiceGateway.tryPay(cart.getSessionId(), totalMinor)
                .onErrorMap(PaymentServiceUnavailableException.class, exception -> new ServiceException("Payment service is unavailable"))
                .flatMap(paid -> {
                    if (!paid) {
                        return Mono.error(new InsufficientFundsException(
                                "Insufficient funds for sessionId=" + cart.getSessionId() + ", totalMinor=" + totalMinor
                        ));
                    }

                    Order order = new Order(cart.getSessionId(), totalMinor);

                    return this.orderRepository.save(order)
                            .flatMap(savedOrder -> {
                                List<OrderItem> orderItems = OrderUtils.buildOrderItems(savedOrder, cartRows);

                                return this.orderItemRepository.saveAll(orderItems)
                                        .then(this.clearCart(cart))
                                        .thenReturn(savedOrder.getId());
                            });
                });
    }

    private Mono<Void> clearCart(Cart cart) {
        return this.cartItemRepository.deleteByCartId(cart.getId())
                .then(Mono.defer(() -> {
                    cart.setStatus(CartStatus.ORDERED);
                    return this.cartRepository.save(cart).then();
                }))
                .then(this.cartCacheInvalidator.invalidateCart(cart.getSessionId()));
    }

}
