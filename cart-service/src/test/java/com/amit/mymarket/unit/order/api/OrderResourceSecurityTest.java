package com.amit.mymarket.unit.order.api;

import com.amit.mymarket.common.configuration.SecurityConfiguration;
import com.amit.mymarket.order.api.OrderResource;
import com.amit.mymarket.order.api.dto.OrderDto;
import com.amit.mymarket.order.usecase.OrderUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest(controllers = OrderResource.class)
@Import(value = {SecurityConfiguration.class, OrderResourceSecurityTest.TestMockConfiguration.class})
class OrderResourceSecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderUseCase orderUseCase;

    @Test
    @DisplayName(value = "GET /orders without authentication should redirect to login")
    void getOrdersByUserId_shouldRedirectToLogin_whenAnonymous() {
        this.webTestClient.get()
                .uri("/orders")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login.*");
    }

    @Test
    @DisplayName(value = "GET /orders with authentication should return 200 OK")
    void getOrdersByUserId_shouldReturnOk_whenAuthenticated() {
        when(this.orderUseCase.getOrdersByUserId(eq("user"))).thenReturn(Mono.just(Collections.emptyList()));

        this.webTestClient
                .mutateWith(mockUser("user"))
                .get()
                .uri("/orders")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName(value = "GET /orders/{id} without authentication should redirect to login")
    void getOrderByIdForUser_shouldRedirectToLogin_whenAnonymous() {
        this.webTestClient.get()
                .uri("/orders/10")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login.*");
    }

    @Test
    @DisplayName(value = "GET /orders/{id} with authentication should return 200 OK")
    void getOrderByIdForUser_shouldReturnOk_whenAuthenticated() {
        var fakeOrder = new Object();

        when(this.orderUseCase.getOrderByIdForUserId(eq("user"), eq(10L)))
                .thenReturn(Mono.just(new OrderDto(1L, Collections.emptyList(), "0")));

        this.webTestClient
                .mutateWith(mockUser("user"))
                .get()
                .uri("/orders/10?newOrder=false")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName(value = "POST /orders without authentication should redirect to login")
    void createOrderFromActiveCartAndClear_shouldRedirectToLogin_whenAnonymous() {
        this.webTestClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login.*");
    }

    @Test
    @DisplayName(value = "POST /orders with authentication should redirect to created order")
    void createOrderFromActiveCartAndClear_shouldRedirectToCreatedOrder_whenAuthenticated() {
        when(this.orderUseCase.createOrderFromActiveCartAndClear(eq("user"))).thenReturn(Mono.just(42L));

        this.webTestClient
                .mutateWith(mockUser("user"))
                .post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/42?newOrder=true");
    }

    @TestConfiguration
    static class TestMockConfiguration {

        @Bean
        OrderUseCase orderUseCase() {
            return Mockito.mock(OrderUseCase.class);
        }

    }

}