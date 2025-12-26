package com.amit.mymarket.order.usecase.impl;

import com.amit.mymarket.order.api.dto.OrderDto;
import com.amit.mymarket.order.service.CheckoutService;
import com.amit.mymarket.order.service.OrderQueryService;
import com.amit.mymarket.order.usecase.OrderUseCase;
import com.amit.mymarket.order.usecase.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderUseCaseFacade implements OrderUseCase {

    private final OrderQueryService orderQueryService;

    private final CheckoutService checkoutService;

    private final OrderMapper orderMapper;

    @Autowired
    public OrderUseCaseFacade(OrderQueryService orderQueryService, CheckoutService checkoutService, OrderMapper orderMapper) {
        this.orderQueryService = orderQueryService;
        this.checkoutService = checkoutService;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<List<OrderDto>> getOrdersByUserId(String userId) {
        return this.orderQueryService.getOrdersByUserId(userId)
                .flatMap(order ->
                        this.orderQueryService.getOrderItems(order.getId())
                                .collectList()
                                .map(items -> this.orderMapper.toOrderDto(order, items))
                )
                .collectList();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<OrderDto> getOrderByIdForUserId(String userId, long orderId) {
        return this.orderQueryService.getOrderByIdForUserId(orderId, userId)
                .flatMap(order ->
                        this.orderQueryService.getOrderItems(order.getId())
                                .collectList()
                                .map(items -> this.orderMapper.toOrderDto(order, items))
                );
    }

    @Override
    @Transactional
    public Mono<Long> createOrderFromActiveCartAndClear(String userId) {
        return this.checkoutService.createOrderFromActiveCartAndClear(userId);
    }

}
