package com.amit.mymarket.item.api.mapper;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.domain.entity.Item;
import org.springframework.stereotype.Component;

@Component
public final class DefaultItemMapper implements ItemMapper {

    @Override
    public Item toItem(CreateItemForm createItemForm) {
        return new Item(
                createItemForm.title(),
                createItemForm.description(),
                createItemForm.priceMinor()
        );
    }

    @Override
    public Item toItem(UpdateItemForm updateItemForm) {
        return new Item(
                updateItemForm.title(),
                updateItemForm.description(),
                updateItemForm.priceMinor()
        );
    }

    @Override
    public ItemView toItemView(Item item) {
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImagePath(),
                item.getPriceMinor()
        );
    }

}
