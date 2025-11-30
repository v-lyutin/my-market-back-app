package com.amit.mymarket.order.usecase.mapper;

import com.amit.mymarket.order.api.dto.OrderDto;
import com.amit.mymarket.order.domain.entity.Order;
import com.amit.mymarket.order.domain.entity.OrderItem;

import java.util.List;

public interface OrderMapper {

    OrderDto toOrderDto(Order order, List<OrderItem> items);

}
