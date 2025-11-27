package com.amit.mymarket.item.api;

import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.usecase.ItemUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping(path = "/items")
public class ItemResource {

    private final ItemUseCase itemUseCase;

    @Autowired
    public ItemResource(ItemUseCase itemUseCase) {
        this.itemUseCase = itemUseCase;
    }

    @GetMapping
    public Mono<Rendering> getCatalogPage(@RequestParam(name = "searchQuery", required = false) String search,
                                          @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                          @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                                          Model model,
                                          WebSession webSession) {
        return this.itemUseCase.getCatalogPage(webSession.getId(), search, sort, pageNumber, pageSize)
                .map(catalogPageDto ->
                        Rendering.view("item/items-view")
                                .modelAttribute("items", catalogPageDto.items())
                                .modelAttribute("searchQuery", catalogPageDto.searchQuery())
                                .modelAttribute("sort", catalogPageDto.sort())
                                .modelAttribute("paging", catalogPageDto.paging())
                                .build()
                );
    }

    @PostMapping
    public Mono<Rendering> mutateItemFromItemsPage(@RequestParam(name = "id") long id,
                                                   @RequestParam(name = "action") ItemAction action,
                                                   @RequestParam(name = "searchQuery", required = false) String searchQuery,
                                                   @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                                   @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                                   @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                                                   WebSession webSession) {
        String redirectUrl = UriComponentsBuilder.fromPath("/items")
                .queryParam("searchQuery", searchQuery)
                .queryParam("sort", sort)
                .queryParam("pageNumber", pageNumber)
                .queryParam("pageSize", pageSize)
                .build()
                .toString();
        return this.itemUseCase.mutateItem(webSession.getId(), id, action)
                .thenReturn(Rendering.redirectTo(redirectUrl).build());
    }

    @GetMapping(path = "/{id}")
    public Mono<Rendering> getItemPage(@PathVariable(name = "id") long id, WebSession webSession) {
        return this.itemUseCase.getItem(webSession.getId(), id)
                .map(itemInfoView ->
                        Rendering.view("item/item-view")
                                .modelAttribute("item", itemInfoView)
                                .build()
                );
    }

    @PostMapping(path = "/{id}")
    public Mono<Rendering> mutateItemFromItemPage(@PathVariable(name = "id") long id,
                                                  @RequestParam(name = "action") ItemAction action,
                                                  WebSession webSession) {
        return this.itemUseCase.mutateItem(webSession.getId(), id, action)
                .thenReturn(Rendering.redirectTo("/items/" + id).build());
    }

}
