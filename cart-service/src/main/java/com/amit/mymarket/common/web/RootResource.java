package com.amit.mymarket.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class RootResource {

    @GetMapping(value = "/")
    public Mono<Rendering> root() {
        return Mono.just(Rendering.redirectTo("/items").build());
    }

}
