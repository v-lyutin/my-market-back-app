package com.amit.mymarket.order.usecase.mapper;

import com.amit.mymarket.common.util.PriceFormatter;
import com.amit.mymarket.order.api.dto.OrderDto;
import com.amit.mymarket.order.api.dto.OrderItemDto;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class DefaultOrderMapper implements OrderMapper {

    @Override
    public OrderDto toOrderDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(this::toOrderItemDto)
                .toList();

        String totalFormatted = PriceFormatter.formatPrice(order.getTotalMinor());

        return new OrderDto(
                order.getId(),
                items,
                totalFormatted
        );
    }

    private OrderItemDto toOrderItemDto(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getId().getItemId(),
                orderItem.getTitleSnapshot(),
                PriceFormatter.formatPrice(orderItem.getPriceMinorSnapshot()),
                orderItem.getQuantity()
        );
    }

}
