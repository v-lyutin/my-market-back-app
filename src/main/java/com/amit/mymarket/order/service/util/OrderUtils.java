package com.amit.mymarket.order.service.util;

import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;

import java.util.List;

public final class OrderUtils {

    public static long calculateTotalMinor(List<CartItemRow> cartItemRows) {
        return cartItemRows.stream()
                .mapToLong(cartItemRow -> {
                    Long price = cartItemRow.priceMinor();
                    Integer quantity = cartItemRow.quantity();
                    long safePrice = price != null ? price : 0L;
                    long safeQuantity = quantity != null ? quantity : 0L;
                    return safePrice * safeQuantity;
                })
                .sum();
    }

    public static List<OrderItem> buildOrderItems(Order order, List<CartItemRow> cartItemRows) {
        return cartItemRows.stream()
                .map(cartItemRow -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(order.getId());
                    orderItem.setItemId(cartItemRow.id());
                    orderItem.setTitleSnapshot(cartItemRow.title());
                    orderItem.setPriceMinorSnapshot(cartItemRow.priceMinor() != null ? cartItemRow.priceMinor() : 0L);
                    orderItem.setQuantity(cartItemRow.quantity() != null ? cartItemRow.quantity() : 0);
                    return orderItem;
                })
                .toList();
    }

    private OrderUtils() {
        throw new UnsupportedOperationException();
    }

}
