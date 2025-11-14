package com.amit.mymarket.cart.api;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.cart.api.type.CartAction;
import com.amit.mymarket.cart.usecase.CartUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(path = "/v1/cart/items")
public class CartResource {

    private final CartUseCase cartUseCase;

    @Autowired
    public CartResource(CartUseCase cartUseCase) {
        this.cartUseCase = cartUseCase;
    }

    @GetMapping
    public String getCartItems(Model model, HttpSession httpSession) {
        CartViewDto cart = this.cartUseCase.getCart(httpSession.getId());
        model.addAttribute("items", cart.items());
        model.addAttribute("total", cart.totalFormatted());
        return "cart/cart-view";
    }

    @PostMapping
    public String mutateCartItem(@RequestParam(name = "id") long id,
                                 @RequestParam(name = "action") CartAction cartAction,
                                 HttpSession httpSession) {
        this.cartUseCase.mutateCartItem(httpSession.getId(), id, cartAction);
        return "redirect:/v1/cart/items";
    }

}
