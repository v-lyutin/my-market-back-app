package com.amit.mymarket.unit.item.api;

import com.amit.mymarket.common.configuration.SecurityConfiguration;
import com.amit.mymarket.item.api.ItemResource;
import com.amit.mymarket.item.api.dto.CatalogPageDto;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.Paging;
import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.usecase.ItemUseCase;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest(controllers = ItemResource.class)
@Import(value = {SecurityConfiguration.class, ItemResourceSecurityTest.TestMockConfiguration.class})
class ItemResourceSecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemUseCase itemUseCase;

    @Test
    @DisplayName(value = "GET /items without authentication should return 200 OK")
    void getCatalogPage_shouldReturnOk_whenAnonymous() {
        when(this.itemUseCase.getCatalogPage(isNull(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(new CatalogPageDto(Collections.emptyList(), new Paging(5, 1, true, true), "", SortType.NO)));

        this.webTestClient.get()
                .uri("/items")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName(value = "GET /items/{id} without authentication should return 200 OK")
    void getItemPage_shouldReturnOk_whenAnonymous() {
        when(this.itemUseCase.getItem(isNull(), eq(10L)))
                .thenReturn(Mono.just(new ItemInfoView(1L, "", "", "", "", 0)));

        this.webTestClient.get()
                .uri("/items/10")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName(value = "POST /items without authentication should redirect to login")
    void mutateItemFromItemsPage_shouldRedirectToLogin_whenAnonymous() {
        this.webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=10&action=PLUS&search=abc&sort=NO&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login.*");
    }

    @Test
    @DisplayName(value = "POST /items/{id} without authentication should redirect to login")
    void mutateItemFromItemPage_shouldRedirectToLogin_whenAnonymous() {
        this.webTestClient.post()
                .uri("/items/10")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login.*");
    }

    @Test
    @DisplayName(value = "POST /items with authentication should redirect back to items with query params")
    void mutateItemFromItemsPage_shouldRedirectToItemsWithParams_whenAuthenticated() {
        when(this.itemUseCase.mutateItem(eq("user"), eq(10L), eq(ItemAction.PLUS))).thenReturn(Mono.empty());

        this.webTestClient
                .mutateWith(mockUser("user"))
                .post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=10&action=PLUS&search=abc&sort=NO&pageNumber=2&pageSize=7")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=abc&sort=NO&pageNumber=2&pageSize=7");
    }

    @Test
    @DisplayName(value = "POST /items/{id} with authentication should redirect back to item page")
    void mutateItemFromItemPage_shouldRedirectToItemPage_whenAuthenticated() {
        when(this.itemUseCase.mutateItem(eq("user"), eq(10L), eq(ItemAction.PLUS))).thenReturn(Mono.empty());

        this.webTestClient
                .mutateWith(mockUser("user"))
                .post()
                .uri("/items/10")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/10");
    }

    @TestConfiguration
    static class TestMockConfiguration {

        @Bean
        ItemUseCase itemUseCase() {
            return Mockito.mock(ItemUseCase.class);
        }

    }

}