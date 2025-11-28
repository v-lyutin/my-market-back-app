package com.amit.mymarket.item.api.mapper;

import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.common.service.MediaUrlResolver;
import com.amit.mymarket.common.util.PriceFormatter;
import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class DefaultItemMapper implements ItemMapper {

    private final MediaUrlResolver mediaUrlResolver;

    @Autowired
    public DefaultItemMapper(MediaUrlResolver mediaUrlResolver) {
        this.mediaUrlResolver = mediaUrlResolver;
    }

    @Override
    public Item toItem(CreateItemForm createItemForm) {
        return new Item(
                createItemForm.title(),
                createItemForm.description(),
                PriceFormatter.convertToPriceMinor(createItemForm.formatPrice())
        );
    }

    @Override
    public Item toItem(UpdateItemForm updateItemForm) {
        return new Item(
                updateItemForm.title(),
                updateItemForm.description(),
                PriceFormatter.convertToPriceMinor(updateItemForm.formatPrice())
        );
    }

    @Override
    public ItemView toItemView(Item item) {
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                this.mediaUrlResolver.buildPublicUrl(item.getImagePath()),
                PriceFormatter.formatPrice(item.getPriceMinor())
        );
    }

    @Override
    public ItemInfoView toItemInfoView(Item item, int quantity) {
        return new ItemInfoView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                this.mediaUrlResolver.buildPublicUrl(item.getImagePath()),
                PriceFormatter.formatPrice(item.getPriceMinor()),
                quantity
        );
    }

    @Override
    public ItemInfoView toItemInfoView(CartItemRow cartItemRow) {
        return new ItemInfoView(
                cartItemRow.id(),
                cartItemRow.title(),
                cartItemRow.description(),
                this.mediaUrlResolver.buildPublicUrl(cartItemRow.imagePath()),
                PriceFormatter.formatPrice(cartItemRow.priceMinor()),
                cartItemRow.quantity()
        );
    }

}
