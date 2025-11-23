package com.amit.mymarket.cart.domain.entity;

import com.amit.mymarket.cart.domain.type.CartStatus;
import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(schema = "shop", name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 128)
    private String sessionId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", length = 16)
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartItem> items = new LinkedHashSet<>();

    public Cart() {}

    @PrePersist
    void prePersist() {
        OffsetDateTime currentOffsetDateTime = OffsetDateTime.now();
        this.createdAt = currentOffsetDateTime;
        this.updatedAt = currentOffsetDateTime;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void addCartItem(CartItem cartItem) {
        this.items.add(cartItem);
        cartItem.setCart(this);
    }

    public void removeCartItem(CartItem cartItem) {
        this.items.remove(cartItem);
        cartItem.setCart(null);
    }

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

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<CartItem> getItems() {
        return Collections.unmodifiableSet(this.items);
    }

    public void setItems(Set<CartItem> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addCartItem);
        }
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
        Cart otherCart = (Cart) otherObject;
        return this.id != null && this.id.equals(otherCart.id);
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
                ", createdAt=" + this.createdAt +
                ", updatedAt=" + this.updatedAt +
                '}';
    }

}
