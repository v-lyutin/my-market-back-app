package com.amit.mymarket.item.api.mapper;

import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.domain.entity.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public final class DefaultItemMapper implements ItemMapper {

    @Override
    public Item toItem(CreateItemForm createItemForm) {
        return new Item(
                createItemForm.title(),
                createItemForm.description(),
                convertToMinor(createItemForm.formatPrice())
        );
    }

    @Override
    public Item toItem(UpdateItemForm updateItemForm) {
        return new Item(
                updateItemForm.title(),
                updateItemForm.description(),
                convertToMinor(updateItemForm.formatPrice())
        );
    }

    @Override
    public ItemView toItemView(Item item) {
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImagePath(),
                formatPrice(item.getPriceMinor())
        );
    }

    @Override
    public ItemInfoView toItemInfoView(Item item, int quantity) {
        return new ItemInfoView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImagePath(),
                formatPrice(item.getPriceMinor()),
                quantity
        );
    }


    private static String formatPrice(Long minor) {
        if (minor == null) {
            return "0";
        }
        BigDecimal price = BigDecimal.valueOf(minor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return price.toString();
    }

    private static long convertToMinor(String price) {
        return new BigDecimal(price)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

}
