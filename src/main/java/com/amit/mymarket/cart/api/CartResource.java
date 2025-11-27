package com.amit.mymarket.cart.api;

import com.amit.mymarket.cart.api.type.CartAction;
import com.amit.mymarket.cart.usecase.CartUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return this.cartUseCase.getCart(webSession.getId())
                .map(cart ->
                        Rendering.view("cart/cart-view")
                                .modelAttribute("items", cart.items())
                                .modelAttribute("total", cart.totalFormatted())
                                .build()
                );
    }

    @PostMapping
    public Mono<Rendering> mutateCartItem(@RequestParam(name = "id") long id,
                                          @RequestParam(name = "action") CartAction cartAction,
                                          WebSession webSession) {
        return this.cartUseCase.mutateCartItem(webSession.getId(), id, cartAction)
                .thenReturn(Rendering.redirectTo("/cart/items").build());
    }

}
