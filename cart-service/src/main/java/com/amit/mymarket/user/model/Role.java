package com.amit.mymarket.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "roles")
public class Role {

    @Id
    private Long id;

    @Column(value = "name")
    private String name;

    public Role() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }
        Role otherRole = (Role) otherObject;
        return Objects.equals(this.id, otherRole.id) && Objects.equals(this.name, otherRole.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + this.id +
                ", name='" + this.name +
                '}';
    }

}
