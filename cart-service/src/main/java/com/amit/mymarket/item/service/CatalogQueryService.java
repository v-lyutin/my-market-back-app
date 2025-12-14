package com.amit.mymarket.item.service;

import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.service.type.SortType;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CatalogQueryService {

    /**
     * Returns a page of catalog items according to searchQuery/sort/pagination.
     * No cart quantities are embedded here.
     */
    Mono<Page<Item>> getCatalogPage(String searchQuery, SortType sortType, int pageNumber, int pageSize);

    /**
     * Returns the current quantity in cart for a single item of the given session.
     * If item is not present in cart, returns 0.
     */
    Mono<Integer> getCartQuantityForItem(String sessionId, long itemId);

    /**
     * Returns quantities in cart for a batch of items of the given session.
     * Missing items must be treated as 0 in the returned map.
     */
    Mono<Map<Long, Integer>> getCartQuantitiesForItems(String sessionId, List<Long> itemIds);

    /**
     * Returns an item by id or throws NotFoundException if not present.
     */
    Mono<Item> getItemById(long itemId);

}
