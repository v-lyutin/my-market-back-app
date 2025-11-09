package com.amit.mymarket.order.repository.projection;

public interface OrderItemSnapshotRow {

    Long getId();

    String getTitle();

    Long getPrice();

    Integer getCount();

}
