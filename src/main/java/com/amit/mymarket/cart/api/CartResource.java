package com.amit.mymarket.cart.api;

import com.amit.mymarket.cart.api.dto.MutateCartItemForm;
import com.amit.mymarket.cart.api.type.CartAction;
import com.amit.mymarket.cart.usecase.CartUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping(path = "/cart/items")
public class CartResource {

    private final CartUseCase cartUseCase;

    @Autowired
    public CartResource(CartUseCase cartUseCase) {
        this.cartUseCase = cartUseCase;
    }

    @GetMapping
    public Mono<Rendering> getCart(WebSession webSession) {
        webSession.getAttributes().put("init", true);
        return this.cartUseCase.getCart(webSession.getId())
                .map(cart ->
                        Rendering.view("cart/cart-view")
                                .modelAttribute("items", cart.items())
                                .modelAttribute("total", cart.totalFormatted())
                                .build()
                );
    }

    @PostMapping
    public Mono<Rendering> mutateCartItem(@ModelAttribute MutateCartItemForm form,
                                          WebSession webSession) {
        webSession.getAttributes().put("init", true);
        return this.cartUseCase.mutateCartItem(webSession.getId(), form.id(), form.action())
                .thenReturn(Rendering.redirectTo("/cart/items").build());
    }

}
