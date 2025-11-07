package com.amit.mymarket.cart.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CartItemId implements Serializable {

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    public CartItemId() {}

    public CartItemId(Long cartId, Long itemId) {
        this.cartId = cartId;
        this.itemId = itemId;
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

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof CartItemId otherCartItemId)) {
            return false;
        }
        return Objects.equals(this.cartId, otherCartItemId.cartId) && Objects.equals(this.itemId, otherCartItemId.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.cartId, this.itemId);
    }

    @Override
    public String toString() {
        return "CartItemId{" +
                "cartId=" + this.cartId +
                ", itemId=" + this.itemId +
                '}';
    }

}
