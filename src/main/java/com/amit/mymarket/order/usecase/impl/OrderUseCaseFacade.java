package com.amit.mymarket.order.usecase.impl;

import com.amit.mymarket.order.api.dto.OrderDto;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.service.CheckoutService;
import com.amit.mymarket.order.service.OrderQueryService;
import com.amit.mymarket.order.usecase.OrderUseCase;
import com.amit.mymarket.order.usecase.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<OrderDto> getOrders(String sessionId) {
        List<Order> orders = this.orderQueryService.fetchOrdersBySession(sessionId);
        return orders.stream()
                .map(this.orderMapper::toOrderDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrder(String sessionId, long orderId) {
        Order order = this.orderQueryService.fetchOrderByIdForSession(orderId, sessionId);
        return this.orderMapper.toOrderDto(order);
    }

    @Override
    @Transactional
    public long checkout(String sessionId) {
        return this.checkoutService.createOrderFromActiveCartAndClear(sessionId);
    }

}
