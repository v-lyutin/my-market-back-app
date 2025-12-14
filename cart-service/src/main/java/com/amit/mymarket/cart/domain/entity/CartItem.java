package com.amit.mymarket.cart.domain.entity;

import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "carts_items")
public class CartItem {

    @Id
    private Long id;

    @Column(value = "cart_id")
    private Long cartId;

    @Column(value = "item_Id")
    private Long itemId;

    @Min(value = 1)
    @Column(value = "quantity")
    private int quantity;

    public CartItem() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCartId() {
        return this.cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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
        CartItem otherCartItem = (CartItem) otherObject;
        return Objects.equals(this.id, otherCartItem.id)
                && Objects.equals(this.cartId, otherCartItem.cartId)
                && Objects.equals(this.itemId, otherCartItem.itemId)
                && this.quantity == otherCartItem.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.cartId, this.itemId, this.quantity);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + this.id +
                ", cartId=" + this.cartId +
                ", itemId=" + this.itemId +
                ", quantity=" + this.quantity +
                '}';
    }

}
