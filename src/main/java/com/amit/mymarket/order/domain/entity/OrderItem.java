package com.amit.mymarket.order.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "orders_items")
public class OrderItem {

    @Id
    private Long id;

    @Column(value = "order_id")
    private Long orderId;

    @Column(value = "item_id")
    private Long itemId;

    @Column(value = "title_snapshot")
    private String titleSnapshot;

    @Column(value = "price_minor_snapshot")
    private long priceMinorSnapshot;

    @Column(value = "quantity")
    private int quantity;

    public OrderItem() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getTitleSnapshot() {
        return this.titleSnapshot;
    }

    public void setTitleSnapshot(String titleSnapshot) {
        this.titleSnapshot = titleSnapshot;
    }

    public long getPriceMinorSnapshot() {
        return this.priceMinorSnapshot;
    }

    public void setPriceMinorSnapshot(long priceMinorSnapshot) {
        this.priceMinorSnapshot = priceMinorSnapshot;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }
        OrderItem otherOrderItem = (OrderItem) otherObject;
        return Objects.equals(this.id, otherOrderItem.id)
                && Objects.equals(this.orderId, otherOrderItem.orderId)
                && Objects.equals(this.itemId, otherOrderItem.itemId)
                && Objects.equals(this.titleSnapshot, otherOrderItem.titleSnapshot)
                && this.priceMinorSnapshot == otherOrderItem.priceMinorSnapshot
                && this.quantity == otherOrderItem.quantity;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + this.id +
                ", orderId=" + this.orderId +
                ", itemId=" + this.itemId +
                ", titleSnapshot='" + this.titleSnapshot +
                ", priceMinorSnapshot=" + this.priceMinorSnapshot +
                ", quantity=" + this.quantity +
                '}';
    }

}
