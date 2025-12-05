package com.amit.mymarket.order.service.impl;

import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.util.SessionUtils;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;
import com.amit.mymarket.order.repository.OrderItemRepository;
import com.amit.mymarket.order.repository.OrderRepository;
import com.amit.mymarket.order.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DefaultOrderQueryService implements OrderQueryService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public DefaultOrderQueryService(OrderRepository orderRepository,
                                    OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public Flux<Order> getOrdersBySession(String sessionId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMapMany(this.orderRepository::findAllBySessionId);
    }

    @Override
    public Mono<Order> getOrderByIdForSession(long orderId, String sessionId) {
        return SessionUtils.ensureSessionId(sessionId)
                .flatMap(id ->
                        this.orderRepository.findByIdAndSessionId(orderId, id)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found for session: id=" + orderId)))
                );
    }

    @Override
    public Flux<OrderItem> getOrderItems(long orderId) {
        return this.orderItemRepository.findAllByOrderId(orderId);
    }

}
