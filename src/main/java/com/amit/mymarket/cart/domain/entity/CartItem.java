package com.amit.mymarket.cart.domain.entity;

import com.amit.mymarket.item.entity.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.Hibernate;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "shop", name = "carts_items")
public class CartItem {

    @EmbeddedId
    private CartItemId id = new CartItemId();

    @MapsId(value = "cartId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_carts_items_carts"))
    private Cart cart;

    @MapsId(value = "itemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_carts_items_items"))
    private Item item;

    @Min(value = 1)
    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public CartItem() {}

    @PrePersist
    @PreUpdate
    void prePersistAndPreUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public CartItemId getId() {
        return this.id;
    }

    public void setId(CartItemId id) {
        this.id = id;
    }

    public Cart getCart() {
        return this.cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
        CartItem otherCartItem = (CartItem) otherObject;
        return this.id != null && this.id.equals(otherCartItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + this.id +
                ", quantity=" + this.quantity +
                ", updatedAt=" + this.updatedAt +
                '}';
    }

}
