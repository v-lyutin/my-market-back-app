package com.amit.mymarket.item.api.mapper;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.entity.Item;

public interface ItemMapper {

    Item toItem(CreateItemForm createItemForm);

    Item toItem(UpdateItemForm updateItemForm);

    ItemView toItemView(Item item);

    ItemInfoView toItemInfoView(Item item, int quantity);

}
