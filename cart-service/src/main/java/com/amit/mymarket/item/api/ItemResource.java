package com.amit.mymarket.item.api;

import com.amit.mymarket.item.api.dto.ItemActionForm;
import com.amit.mymarket.item.api.dto.MutateItemForm;
import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.usecase.ItemUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    public Mono<Rendering> getCatalogPage(@RequestParam(name = "search", required = false) String search,
                                          @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                          @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                                          WebSession webSession) {

        webSession.getAttributes().put("init", true);

        return this.itemUseCase.getCatalogPage(webSession.getId(), search, sort, pageNumber, pageSize)
                .map(catalogPageDto ->
                        Rendering.view("item/items-view")
                                .modelAttribute("items", catalogPageDto.items())
                                .modelAttribute("search", catalogPageDto.searchQuery())
                                .modelAttribute("sort", catalogPageDto.sort())
                                .modelAttribute("paging", catalogPageDto.paging())
                                .build()
                );
    }

    @PostMapping(path = "/{id}")
    public Mono<Rendering> mutateItemFromItemPage(@PathVariable(name = "id") long id,
                                                  @ModelAttribute ItemActionForm form,
                                                  WebSession webSession) {

        webSession.getAttributes().put("init", true);

        return this.itemUseCase.mutateItem(webSession.getId(), id, form.action())
                .thenReturn(Rendering.redirectTo("/items/" + id).build());
    }

    @PostMapping
    public Mono<Rendering> mutateItemFromItemsPage(@ModelAttribute MutateItemForm form,
                                                   WebSession webSession) {
        webSession.getAttributes().put("init", true);

        Long id = form.id();
        ItemAction action = form.action();
        String search = form.search();
        SortType sort = form.sort() != null ? form.sort() : SortType.NO;
        int pageNumber = form.pageNumber() != null ? form.pageNumber() : 1;
        int pageSize = form.pageSize() != null ? form.pageSize() : 5;

        String redirectUrl = UriComponentsBuilder.fromPath("/items")
                .queryParam("search", search)
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

        webSession.getAttributes().put("init", true);

        return this.itemUseCase.getItem(webSession.getId(), id)
                .map(itemInfoView ->
                        Rendering.view("item/item-view")
                                .modelAttribute("item", itemInfoView)
                                .build()
                );
    }

}
