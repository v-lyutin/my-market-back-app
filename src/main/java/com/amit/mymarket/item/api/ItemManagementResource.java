package com.amit.mymarket.item.api;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.usecase.ItemManagementUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping(path = "/management/items")
public class ItemManagementResource {

    private final ItemManagementUseCase itemManagementUseCase;

    @Autowired
    public ItemManagementResource(ItemManagementUseCase itemManagementUseCase) {
        this.itemManagementUseCase = itemManagementUseCase;
    }

    @GetMapping(path = "/new")
    public Mono<Rendering> showCreateForm() {
        CreateItemForm form = new CreateItemForm("", "", null);
        return Mono.just(
                Rendering.view("item/management/form/create-item-form")
                        .modelAttribute("form", form)
                        .build()
        );
    }

    @PostMapping
    public Mono<Rendering> createItemAndOptionallyUploadImage(@Valid @ModelAttribute(value = "form") CreateItemForm form,
                                                              @RequestPart(value = "file", required = false) FilePart file) {
        return this.itemManagementUseCase.createItemAndOptionallyUploadImage(form, file)
                .map(item -> Rendering.redirectTo("/management/items/" + item.id()).build());
    }

    @GetMapping(path = "/{id}")
    public Mono<Rendering> getItemById(@PathVariable long id) {
        return this.itemManagementUseCase.getItemById(id)
                .map(item ->
                        Rendering.view("item/management/view/item-management-view")
                                .modelAttribute("item", item)
                                .build()
                );
    }

    @GetMapping(path = "/{id}/edit")
    public Mono<Rendering> showEditForm(@PathVariable long id) {
        return this.itemManagementUseCase.getItemById(id)
                .map(item ->
                        Rendering.view("item/management/form/edit-item-form")
                                .modelAttribute("itemId", id)
                                .modelAttribute("form", new UpdateItemForm(
                                        item.title(),
                                        item.description(),
                                        item.formatPrice()
                                ))
                                .build()
                );
    }

    @PostMapping(path = "/{id}")
    public Mono<Rendering> updateItemAttributes(@PathVariable long id,
                                                @Valid @ModelAttribute(value = "form") UpdateItemForm form) {
        return this.itemManagementUseCase.updateItemAttributes(id, form)
                .thenReturn(Rendering.redirectTo("/management/items/" + id).build());
    }

    @PostMapping(path = "/{id}/image")
    public Mono<Rendering> replaceItemImage(@PathVariable long id,
                                            @RequestParam(value = "file") FilePart file) {
        return this.itemManagementUseCase.replaceItemImage(id, file)
                .thenReturn(Rendering.redirectTo("/management/items/" + id).build());
    }

    @PostMapping(path = "/{id}/delete")
    public Mono<Rendering> deleteItem(@PathVariable long id) {
        return this.itemManagementUseCase.deleteItem(id)
                .thenReturn(Rendering.redirectTo("/management/items/new").build());
    }

}
