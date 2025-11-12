package com.amit.mymarket.item.api;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.usecase.ItemManagementUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(path = "/v1/management/items")
public class ItemManagementResource {

    private final ItemManagementUseCase itemManagementUseCase;

    @Autowired
    public ItemManagementResource(ItemManagementUseCase itemManagementUseCase) {
        this.itemManagementUseCase = itemManagementUseCase;
    }

    @GetMapping(path = "/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new CreateItemForm("", "", null));
        return "management/create-item-form";
    }

    @PostMapping
    public String createItemAndOptionallyUploadImage(@Valid @ModelAttribute(value = "form") CreateItemForm form,
                                                     @RequestParam(value = "file", required = false) MultipartFile file,
                                                     RedirectAttributes redirectAttributes,
                                                     Model model) {
        ItemView itemResponse = this.itemManagementUseCase.createItemAndOptionallyUploadImage(form, file);
        redirectAttributes.addFlashAttribute("success", "Item created successfully");
        return "redirect:/v1/management/items/" + itemResponse.id();
    }

    @GetMapping(path = "/{id}")
    public String fetchItemById(@PathVariable long id, Model model) {
        ItemView item = this.itemManagementUseCase.fetchItemById(id);
        model.addAttribute("item", item);
        return "management/item-view";
    }

    @GetMapping(path = "/{id}/edit")
    public String showEditForm(@PathVariable long id, Model model) {
        ItemView item = this.itemManagementUseCase.fetchItemById(id);
        model.addAttribute("itemId", id);
        model.addAttribute("form", new UpdateItemForm(
                item.title(),
                item.description(),
                item.priceMinor()
        ));
        return "management/edit-item-form";
    }

    @PostMapping(path = "/{id}")
    public String updateItemAttributes(@PathVariable long id,
                                       @Valid @ModelAttribute(value = "form") UpdateItemForm form,
                                       RedirectAttributes redirectAttributes) {
        this.itemManagementUseCase.updateItemAttributes(id, form);
        redirectAttributes.addFlashAttribute("success", "Изменения сохранены");
        return "redirect:/v1/management/items/" + id;
    }

    @PostMapping(path = "/{id}/image")
    public String replacePrimaryItemImage(@PathVariable long id,
                                          @RequestParam(value = "file") MultipartFile file,
                                          RedirectAttributes redirectAttributes) {
        this.itemManagementUseCase.replacePrimaryItemImage(id, file);
        redirectAttributes.addFlashAttribute("success", "Изображение заменено");
        return "redirect:/v1/management/items/" + id;
    }

    @PostMapping(path = "/{id}/delete")
    public String deleteItemCompletely(@PathVariable long id, RedirectAttributes redirectAttributes) {
        this.itemManagementUseCase.deleteItemCompletely(id);
        redirectAttributes.addFlashAttribute("success", "Товар удалён");
        return "redirect:/v1/management/items/new";
    }

}
