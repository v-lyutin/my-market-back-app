package com.amit.mymarket.order.service.impl;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.CartRepository;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;
import com.amit.mymarket.order.domain.type.OrderStatus;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.service.CheckoutService;
import com.amit.mymarket.order.service.exception.EmptyCartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class DefaultCheckoutService implements CheckoutService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final OrderRepository orderRepository;

    @Autowired
    public DefaultCheckoutService(CartRepository cartRepository, CartItemRepository cartItemRepository, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public long createOrderFromActiveCartAndClear(String sessionId) {
        Cart cart = requireActiveNonEmptyCart(sessionId);

        Order order = this.newOrderHeader(sessionId);
        List<CartItem> cartItems = new ArrayList<>(cart.getItems());

        List<OrderItem> orderItems = this.toOrderItems(order, cartItems);
        long totalMinor = orderItems.stream()
                .mapToLong(orderItem -> orderItem.getPriceMinorSnapshot() * (long) orderItem.getQuantity())
                .sum();

        orderItems.forEach(order::addOrderItem);
        order.setTotalMinor(totalMinor);

        Order persistedOrder = this.orderRepository.save(order);

        this.clearCart(cart, cartItems);
        return persistedOrder.getId();
    }

    private Cart requireActiveNonEmptyCart(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot create order: the active cart is empty");
        }
        return cart;
    }

    private Order newOrderHeader(String sessionId) {
        Order order = new Order();
        order.setSessionId(sessionId);
        order.setStatus(OrderStatus.CREATED);
        return order;
    }

    private List<OrderItem> toOrderItems(Order order, Collection<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> {
                    Item item = cartItem.getItemId();
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(order);
                    orderItem.setItemId(item);
                    orderItem.setTitleSnapshot(item.getTitle());
                    orderItem.setPriceMinorSnapshot(item.getPriceMinor());
                    orderItem.setQuantity(cartItem.getQuantity());
                    return orderItem;
                })
                .toList();
    }

    private void clearCart(Cart cart, Collection<CartItem> cartItems) {
        cartItems.forEach(cartItem -> this.cartItemRepository.deleteCartItem(cart.getId(), cartItem.getItemId().getId()));
        cart.setStatus(CartStatus.ORDERED);
        this.cartRepository.save(cart);
    }

}
