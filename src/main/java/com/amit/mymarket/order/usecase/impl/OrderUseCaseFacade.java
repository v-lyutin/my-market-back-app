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
    public Mono<List<OrderDto>> getOrdersBySession(String sessionId) {
        return this.orderQueryService.getOrdersBySession(sessionId)
                .flatMap(order ->
                        this.orderQueryService.getOrderItems(order.getId())
                                .collectList()
                                .map(items -> this.orderMapper.toOrderDto(order, items))
                )
                .collectList();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<OrderDto> getOrderByIdForSession(String sessionId, long orderId) {
        return this.orderQueryService.getOrderByIdForSession(orderId, sessionId)
                .flatMap(order ->
                        this.orderQueryService.getOrderItems(order.getId())
                                .collectList()
                                .map(items -> this.orderMapper.toOrderDto(order, items))
                );
    }

    @Override
    @Transactional
    public Mono<Long> createOrderFromActiveCartAndClear(String sessionId) {
        return this.checkoutService.createOrderFromActiveCartAndClear(sessionId);
    }

}
