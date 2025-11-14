package com.amit.mymarket.order.service.impl;

import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.repository.projection.OrderHeaderRow;
import com.amit.mymarket.order.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DefaultOrderQueryService implements OrderQueryService {

    private final OrderRepository orderRepository;

    @Autowired
    public DefaultOrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> fetchOrdersBySession(String sessionId) {
        List<Long> orderIds = this.orderRepository.findOrdersBySession(sessionId).stream()
                .map(OrderHeaderRow::getId)
                .toList();
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Order> ordersById = this.orderRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));
        return orderIds.stream()
                .map(ordersById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Order fetchOrderByIdForSession(long orderId, String sessionId) {
        Optional<OrderHeaderRow> orderHeaderRow = this.orderRepository.findOrderHeader(orderId, sessionId);
        if (orderHeaderRow.isEmpty()) {
            throw new ResourceNotFoundException("Order not found for session: id=" + orderId);
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order disappeared: id=" + orderId));
    }

}
