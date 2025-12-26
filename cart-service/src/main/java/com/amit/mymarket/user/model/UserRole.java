package com.amit.mymarket.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "users_roles")
public class UserRole {

    @Id
    private Long id;

    @Column(value = "user_id")
    private Long userId;

    @Column(value = "role_id")
    private Long roleId;

    public UserRole() {}

    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return this.roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }
        UserRole otherUserRole = (UserRole) otherObject;
        return Objects.equals(this.id, otherUserRole.id)
                && Objects.equals(this.userId, otherUserRole.userId)
                && Objects.equals(this.roleId, otherUserRole.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.userId, this.roleId);
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + this.id +
                ", userId=" + this.userId +
                ", roleId=" + this.roleId +
                '}';
    }

}
