package com.amit.mymarket.order.api;

import com.amit.mymarket.order.api.dto.OrderDto;
import com.amit.mymarket.order.usecase.OrderUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path = "/v1/orders")
public class OrderResource {

    private final OrderUseCase orderUseCase;

    @Autowired
    public OrderResource(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @GetMapping
    public String getOrders(Model model, HttpSession httpSession) {
        List<OrderDto> orders = this.orderUseCase.getOrders(httpSession.getId());
        model.addAttribute("orders", orders);
        return "order/orders-view";
    }

    @GetMapping(path = "/{id}")
    public String getOrder(@PathVariable(name = "id") long id,
                           @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder,
                           Model model,
                           HttpSession httpSession) {
        OrderDto order = this.orderUseCase.getOrder(httpSession.getId(), id);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order/order-view";
    }

    @PostMapping
    public String checkout(HttpSession httpSession) {
        long newOrderId = this.orderUseCase.checkout(httpSession.getId());
        return "redirect:/v1/orders/" + newOrderId + "?newOrder=true";
    }

}
