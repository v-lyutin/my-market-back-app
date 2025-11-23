package com.amit.mymarket.item.api;

import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.CatalogPageDto;
import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.usecase.ItemUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/v1/items")
public class ItemResource {

    private final ItemUseCase itemUseCase;

    @Autowired
    public ItemResource(ItemUseCase itemUseCase) {
        this.itemUseCase = itemUseCase;
    }

    @GetMapping
    public String getItemsPage(@RequestParam(name = "search", required = false) String search,
                               @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                               @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                               @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                               Model model,
                               HttpSession httpSession) {
        String sessionId = httpSession.getId();
        CatalogPageDto catalogPageDto = this.itemUseCase.getCatalogPage(sessionId, search, sort, pageNumber, pageSize);
        model.addAttribute("items", catalogPageDto.items());
        model.addAttribute("search", catalogPageDto.search());
        model.addAttribute("sort", catalogPageDto.sort());
        model.addAttribute("paging", catalogPageDto.paging());
        return "item/items-view";
    }

    @PostMapping
    public String mutateItemFromItemsPage(@RequestParam(name = "id") long id,
                                          @RequestParam(name = "action") ItemAction action,
                                          @RequestParam(name = "search", required = false) String search,
                                          @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                          @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                                          HttpSession httpSession) {
        String sessionId = httpSession.getId();
        this.itemUseCase.mutateItem(sessionId, id, action);
        return "redirect:/v1/items?search=" + (search == null ? "" : search)
                + "&sort=" + sort
                + "&pageNumber=" + pageNumber
                + "&pageSize=" + pageSize;
    }

    @GetMapping(path = "/{id}")
    public String getItemPage(@PathVariable(name = "id") long id, Model model, HttpSession httpSession) {
        String sessionId = httpSession.getId();
        ItemInfoView itemInfoView = this.itemUseCase.getItem(sessionId, id);
        model.addAttribute("item", itemInfoView);
        return "item/item-view";
    }

    @PostMapping(path = "/{id}")
    public String mutateItemFromItemPage(@PathVariable(name = "id") long id,
                                         @RequestParam(name = "action") ItemAction action,
                                         HttpSession httpSession) {
        String sessionId = httpSession.getId();
        this.itemUseCase.mutateItem(sessionId, id, action);
        return "redirect:/v1/items/" + id;
    }

}
