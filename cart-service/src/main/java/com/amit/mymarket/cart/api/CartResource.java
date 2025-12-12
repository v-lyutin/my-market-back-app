package com.amit.mymarket.cart.api;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.cart.api.dto.MutateCartItemForm;
import com.amit.mymarket.cart.api.type.CartAction;
import com.amit.mymarket.cart.usecase.CartUseCase;
import com.amit.mymarket.order.service.CheckoutService;
import com.amit.mymarket.order.service.model.CheckoutAvailability;
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

    private final CheckoutService checkoutService;

    @Autowired
    public CartResource(CartUseCase cartUseCase, CheckoutService checkoutService) {
        this.cartUseCase = cartUseCase;
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public Mono<Rendering> getCart(WebSession webSession) {
        webSession.getAttributes().put("init", true);

        Mono<CartViewDto> cartViewDto = this.cartUseCase.getCart(webSession.getId());
        Mono<CheckoutAvailability> checkoutAvailability = this.checkoutService.getCheckoutAvailability(webSession.getId());

        return Mono.zip(cartViewDto, checkoutAvailability)
                .map(tuple -> {
                    CartViewDto cart = tuple.getT1();
                    CheckoutAvailability availability = tuple.getT2();
                    return Rendering.view("cart/cart-view")
                            .modelAttribute("items", cart.items())
                            .modelAttribute("total", cart.totalFormatted())
                            .modelAttribute("checkoutEnabled", availability.enabled())
                            .modelAttribute("checkoutMessage", availability.message())
                            .build();
                });
    }

    @PostMapping
    public Mono<Rendering> mutateCartItem(@ModelAttribute MutateCartItemForm form,
                                          WebSession webSession) {
        webSession.getAttributes().put("init", true);
        return this.cartUseCase.mutateCartItem(webSession.getId(), form.id(), form.action())
                .thenReturn(Rendering.redirectTo("/cart/items").build());
    }

}
