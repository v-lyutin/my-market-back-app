package com.amit.mymarket.item.repository.projection;

public interface ItemWithCountRow {

    Long getId();

    String getTitle();

    String getDescription();

    String getImagePath();

    Long getPriceMinor();

    Integer getCount();

}
