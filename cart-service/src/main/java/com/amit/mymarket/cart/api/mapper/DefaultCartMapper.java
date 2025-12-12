package com.amit.mymarket.cart.api.mapper;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.common.util.PriceFormatter;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class DefaultCartMapper implements CartMapper {

    @Override
    public CartViewDto toCartViewDto(List<ItemInfoView> items, Long totalMinor) {
        return new CartViewDto(
                items,
                PriceFormatter.formatPrice(totalMinor),
                totalMinor
        );
    }

}
