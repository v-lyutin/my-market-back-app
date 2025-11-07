package com.amit.mymarket.item.web;

import com.amit.mymarket.common.web.dto.ItemDto;
import com.amit.mymarket.item.domain.type.ItemAction;
import com.amit.mymarket.common.util.Paging;
import com.amit.mymarket.item.domain.type.SortType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path = "/v1/items")
public class ItemResource {

    @GetMapping
    public String getItemsPage(@RequestParam(name = "search", required = false) String search,
                               @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                               @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                               @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                               Model model) {
        List<List<ItemDto>> chunkedItems = List.of();
        Paging paging = new Paging(pageSize, pageNumber, false, false);
        model.addAttribute("items", chunkedItems);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", paging);
        return "items";
    }

    @PostMapping
    public String mutateItemFromItemsPage(@RequestParam(name = "id") long id,
                                          @RequestParam(name = "action") ItemAction action,
                                          @RequestParam(name = "search", required = false) String search,
                                          @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                          @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                          @RequestParam(name = "pageSize", defaultValue = "5") int pageSize) {
        return "redirect:/items?search=" + (search == null ? "" : search)
                + "&sort=" + sort
                + "&pageNumber=" + pageNumber
                + "&pageSize=" + pageSize;
    }

    @GetMapping(path = "/{id}")
    public String getItemPage(@PathVariable(name = "id") long id, Model model) {
        ItemDto item = new ItemDto(id, "Title", "Desc", "/images/ball.jpg", 199_00, 0);
        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping(path = "/{id}")
    public String mutateItemFromItemPage(@PathVariable(name = "id") long id,
                                         @RequestParam(name = "action") ItemAction action,
                                         Model model) {
        ItemDto item = new ItemDto(id, "Title", "Desc", "/images/ball.jpg", 199_00, 1);
        model.addAttribute("item", item);
        return "item";
    }

}
