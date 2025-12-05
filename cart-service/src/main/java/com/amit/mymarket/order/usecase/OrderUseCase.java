package com.amit.mymarket.order.usecase;

import com.amit.mymarket.order.api.dto.OrderDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderUseCase {

    Mono<List<OrderDto>> getOrdersBySession(String sessionId);

    Mono<OrderDto> getOrderByIdForSession(String sessionId, long orderId);

    Mono<Long> createOrderFromActiveCartAndClear(String sessionId);

}
