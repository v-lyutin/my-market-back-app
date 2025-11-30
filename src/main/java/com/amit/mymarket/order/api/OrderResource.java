package com.amit.mymarket.order.api;

import com.amit.mymarket.order.usecase.OrderUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping(path = "/orders")
public class OrderResource {

    private final OrderUseCase orderUseCase;

    @Autowired
    public OrderResource(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @GetMapping
    public Mono<Rendering> getOrdersBySession(WebSession webSession) {
        webSession.getAttributes().put("init", true);
        return this.orderUseCase.getOrdersBySession(webSession.getId())
                .map(orders ->
                        Rendering.view("order/orders-view")
                                .modelAttribute("orders", orders)
                                .build()
                );
    }

    @GetMapping(path = "/{id}")
    public Mono<Rendering> getOrderByIdForSession(@PathVariable(name = "id") long id,
                                                  @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder,
                                                  WebSession webSession) {
        webSession.getAttributes().put("init", true);
        return this.orderUseCase.getOrderByIdForSession(webSession.getId(), id)
                .map(order ->
                        Rendering.view("order/order-view")
                                .modelAttribute("order", order)
                                .modelAttribute("newOrder", newOrder)
                                .build()
                );
    }

    @PostMapping
    public Mono<Rendering> createOrderFromActiveCartAndClear(WebSession webSession) {
        return this.orderUseCase.createOrderFromActiveCartAndClear(webSession.getId())
                .map(newOrderId -> Rendering.redirectTo("/orders/" + newOrderId + "?newOrder=true").build());
    }

}
