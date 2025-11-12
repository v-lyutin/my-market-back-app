package com.amit.mymarket.item.usecase;

import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.CatalogPageDto;
import com.amit.mymarket.item.domain.type.ItemAction;
import com.amit.mymarket.item.domain.type.SortType;

public interface ItemUseCase {

    CatalogPageDto getCatalogPage(String sessionId,
                                  String search,
                                  SortType sort,
                                  int pageNumber,
                                  int pageSize);

    ItemInfoView getItem(String sessionId, long itemId);

    void mutateItem(String sessionId, long itemId, ItemAction itemAction);

}
