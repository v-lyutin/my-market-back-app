package com.amit.mymarket.order.usecase;

import com.amit.mymarket.order.api.dto.OrderDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderUseCase {

    Mono<List<OrderDto>> getOrdersByUserId(String userId);

    Mono<OrderDto> getOrderByIdForUserId(String userId, long orderId);

    Mono<Long> createOrderFromActiveCartAndClear(String userId);

}
