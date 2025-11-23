package com.amit.mymarket.cart.repository.projection;

public interface CartItemRow {

    Long getId();

    String getTitle();

    String getDescription();

    String getImagePath();

    Long getPriceMinor();

    Integer getCount();

}
