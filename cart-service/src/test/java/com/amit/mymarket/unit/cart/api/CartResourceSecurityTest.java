package com.amit.mymarket.unit.cart.api;

import com.amit.mymarket.cart.api.CartResource;
import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.cart.api.type.CartAction;
import com.amit.mymarket.cart.usecase.CartUseCase;
import com.amit.mymarket.common.configuration.SecurityConfiguration;
import com.amit.mymarket.order.service.CheckoutService;
import com.amit.mymarket.order.service.model.CheckoutAvailability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest(
        controllers = CartResource.class,
        excludeAutoConfiguration = {
                ReactiveOAuth2ClientAutoConfiguration.class,
                ReactiveOAuth2ClientWebSecurityAutoConfiguration.class,
                ReactiveOAuth2ResourceServerAutoConfiguration.class
        })
@Import(value = {SecurityConfiguration.class, CartResourceSecurityTest.TestMockConfiguration.class})
class CartResourceSecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartUseCase cartUseCase;

    @Autowired
    private CheckoutService checkoutService;

    @Test
    @DisplayName(value = "GET /cart/items without authentication should redirect to login")
    void getCart_shouldRedirectToLogin_whenAnonymous() {
        this.webTestClient.get()
                .uri("/cart/items")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login.*");
    }

    @Test
    @DisplayName(value = "GET /cart/items with authentication should return 200 OK")
    void getCart_shouldReturnOk_whenAuthenticated() {
        when(this.cartUseCase.getCart("user")).thenReturn(Mono.just(new CartViewDto(Collections.emptyList(), "0", 0L)));
        when(this.checkoutService.getCheckoutAvailability("user")).thenReturn(Mono.just(new CheckoutAvailability(true, "")));

        this.webTestClient
                .mutateWith(mockUser("user"))
                .get()
                .uri("/cart/items")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName(value = "POST /cart/items without authentication should redirect to login")
    void mutateCartItem_shouldRedirectToLogin_whenAnonymous() {
        this.webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=123&action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login.*");
    }

    @Test
    @DisplayName(value = "POST /cart/items with authentication should redirect back to cart")
    void mutateCartItem_shouldRedirectToCartItems_whenAuthenticated() {
        when(this.cartUseCase.mutateCartItem(eq("user"), anyLong(), eq(CartAction.PLUS))).thenReturn(Mono.empty());

        this.webTestClient
                .mutateWith(mockUser("user"))
                .post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=123&action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");
    }

    @TestConfiguration
    static class TestMockConfiguration {

        @Bean
        ReactiveAuthenticationManager reactiveAuthenticationManager() {
            return authentication -> {
                authentication.setAuthenticated(true);
                return Mono.just(authentication);
            };
        }

        @Bean
        CartUseCase cartUseCase() {
            return Mockito.mock(CartUseCase.class);
        }

        @Bean
        CheckoutService checkoutService() {
            return Mockito.mock(CheckoutService.class);
        }

    }

}