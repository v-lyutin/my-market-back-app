package com.amit.mymarket.cart.web;

import com.amit.mymarket.common.web.dto.ItemInfoDto;
import com.amit.mymarket.cart.domain.type.CartAction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping(path = "/v1/cart/items")
public class CartResource {

    @GetMapping
    public String getCartItems(Model model) {
        List<ItemInfoDto> items = List.of();
        long total = 0L;
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping
    public String mutateCartItem(@RequestParam(name = "id") long id, @RequestParam(name = "action") CartAction action, Model model) {
        model.addAttribute("items", List.of());
        model.addAttribute("total", 0L);
        return "cart";
    }

}
