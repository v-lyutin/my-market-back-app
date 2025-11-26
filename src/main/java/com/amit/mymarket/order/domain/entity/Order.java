package com.amit.mymarket.order.domain.entity;

import com.amit.mymarket.order.domain.type.OrderStatus;
import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "orders")
public class Order {

    @Id
    private Long id;

    @Column(value = "session_id")
    private String sessionId;

    @Column(value = "status")
    private OrderStatus status = OrderStatus.CREATED;

    @Min(value = 0)
    @Column(value = "total_minor")
    private Long totalMinor;

    public Order() {}

    public Order(String sessionId, long totalMinor) {
        this.id = null;
        this.sessionId = sessionId;
        this.totalMinor = totalMinor;
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

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }
        Order otherOrder = (Order) otherObject;
        return Objects.equals(this.id, otherOrder.id)
                && Objects.equals(this.sessionId, otherOrder.sessionId)
                && this.status == otherOrder.status
                && Objects.equals(this.totalMinor, otherOrder.totalMinor);
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
                '}';
    }

}
