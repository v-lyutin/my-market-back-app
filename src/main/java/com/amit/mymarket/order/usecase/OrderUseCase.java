package com.amit.mymarket.order.usecase;

import com.amit.mymarket.order.api.dto.OrderDto;

import java.util.List;

public interface OrderUseCase {

    List<OrderDto> getOrders(String sessionId);

    OrderDto getOrder(String sessionId, long orderId);

    long checkout(String sessionId);

}
