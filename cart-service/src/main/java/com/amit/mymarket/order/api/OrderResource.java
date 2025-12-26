package com.amit.mymarket.order.api;

import com.amit.mymarket.order.usecase.OrderUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Controller
@RequestMapping(path = "/orders")
public class OrderResource {

    private final OrderUseCase orderUseCase;

    @Autowired
    public OrderResource(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @GetMapping
    public Mono<Rendering> getOrdersByUserId(Principal principal) {
        return this.orderUseCase.getOrdersByUserId(principal.getName())
                .map(orders ->
                        Rendering.view("order/orders-view")
                                .modelAttribute("orders", orders)
                                .build()
                );
    }

    @GetMapping(path = "/{id}")
    public Mono<Rendering> getOrderByIdForUser(@PathVariable(name = "id") long id,
                                               @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder,
                                               Principal principal) {
        return this.orderUseCase.getOrderByIdForUserId(principal.getName(), id)
                .map(order ->
                        Rendering.view("order/order-view")
                                .modelAttribute("order", order)
                                .modelAttribute("newOrder", newOrder)
                                .build()
                );
    }

    @PostMapping
    public Mono<Rendering> createOrderFromActiveCartAndClear(Principal principal) {
        return this.orderUseCase.createOrderFromActiveCartAndClear(principal.getName())
                .map(newOrderId -> Rendering.redirectTo("/orders/" + newOrderId + "?newOrder=true").build());
    }

}
