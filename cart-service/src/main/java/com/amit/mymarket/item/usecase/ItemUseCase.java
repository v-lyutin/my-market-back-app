package com.amit.mymarket.item.usecase;

import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.CatalogPageDto;
import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.service.type.SortType;
import reactor.core.publisher.Mono;

public interface ItemUseCase {

    Mono<CatalogPageDto> getCatalogPage(String sessionId, String search, SortType sort, int pageNumber, int pageSize);

    Mono<ItemInfoView> getItem(String sessionId, long itemId);

    Mono<Void> mutateItem(String sessionId, long itemId, ItemAction itemAction);

}
