package com.amit.mymarket.order.domain.entity;

import com.amit.mymarket.order.domain.type.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.Hibernate;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(schema = "shop", name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 128)
    private String sessionId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderStatus status = OrderStatus.CREATED;

    @Min(value = 0)
    @Column(name = "total_minor", nullable = false)
    private Long totalMinor;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    public void addOrderItem(OrderItem orderItem) {
        this.items.add(orderItem);
        orderItem.setOrder(this);
    }
    public void removeOrderItem(OrderItem orderItem) {
        this.items.remove(orderItem);
        orderItem.setOrder(null);
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

    public OrderStatus getStatus() {
        return this.status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getTotalMinor() {
        return this.totalMinor;
    }

    public void setTotalMinor(Long totalMinor) {
        this.totalMinor = totalMinor;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public void setItems(List<OrderItem> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addOrderItem);
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
        Order otherOrder = (Order) otherObject;
        return this.id != null && this.id.equals(otherOrder.id);
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + this.id +
                ", sessionId='" + this.sessionId +
                ", status=" + this.status +
                ", totalMinor=" + this.totalMinor +
                ", createdAt=" + this.createdAt +
                '}';
    }

}
