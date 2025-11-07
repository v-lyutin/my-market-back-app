package com.amit.mymarket.order.web;

import com.amit.mymarket.order.web.dto.OrderDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path = "/v1/orders")
public class OrderResource {

    @GetMapping
    public String getOrders(Model model) {
        List<OrderDto> orders = List.of();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String getOrder(@PathVariable(name = "id") long id,
                           @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder,
                           Model model) {
        OrderDto order = new OrderDto(id, List.of(), 0L);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping
    public String checkout() {
        long newOrderId = 1L;
        return "redirect:/orders/" + newOrderId + "?newOrder=true";
    }

}
