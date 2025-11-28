package com.amit.mymarket.cart.domain.entity;

import com.amit.mymarket.cart.domain.type.CartStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "carts")
public class Cart {

    @Id
    private Long id;

    @Column(value = "session_id")
    private String sessionId;

    @Column(value = "status")
    private CartStatus status = CartStatus.ACTIVE;

    public Cart() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public CartStatus getStatus() {
        return this.status;
    }

    public void setStatus(CartStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }
        Cart otherCart = (Cart) otherObject;
        return Objects.equals(this.id, otherCart.id)
                && Objects.equals(this.sessionId, otherCart.sessionId)
                && this.status == otherCart.status;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + this.id +
                ", sessionId='" + this.sessionId +
                ", status=" + this.status +
                '}';
    }

}
