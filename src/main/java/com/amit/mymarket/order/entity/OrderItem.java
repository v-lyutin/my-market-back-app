package com.amit.mymarket.order.entity;

import com.amit.mymarket.item.domain.entity.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.Hibernate;

@Entity
@Table(schema = "shop", name = "orders_items")
public class OrderItem {

    @EmbeddedId
    private OrderItemId id = new OrderItemId();

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_items_orders"))
    private Order order;

    @MapsId("itemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_items_items"))
    private Item item;

    @Column(name = "title_snapshot", nullable = false, length = 255)
    private String titleSnapshot;

    @Min(value = 0)
    @Column(name = "price_minor_snapshot", nullable = false)
    private long priceMinorSnapshot;

    @Min(value = 1)
    @Column(name = "quantity", nullable = false)
    private int quantity;

    public OrderItem() {}

    public OrderItemId getId() {
        return this.id;
    }

    public void setId(OrderItemId id) {
        this.id = id;
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
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
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(otherObject)) {
            return false;
        }
        OrderItem otherOrderItem = (OrderItem) otherObject;
        return this.id != null && this.id.equals(otherOrderItem.id);
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + this.id +
                ", titleSnapshot='" + this.titleSnapshot +
                ", priceMinorSnapshot=" + this.priceMinorSnapshot +
                ", quantity=" + this.quantity +
                '}';
    }

}
