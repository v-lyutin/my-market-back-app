package com.amit.mymarket.cart.usecase.mapper;

import com.amit.mymarket.cart.api.dto.CartViewDto;
import com.amit.mymarket.item.api.dto.ItemInfoView;

import java.util.List;

public interface CartMapper {

    CartViewDto toCartViewDto(List<ItemInfoView> items, Long totalMinor);

}
