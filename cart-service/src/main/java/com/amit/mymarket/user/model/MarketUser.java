package com.amit.mymarket.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "users")
public class MarketUser {

    @Id
    private Long id;

    @Column(value = "username")
    private String username;

    @Column(value = "password_hash")
    private String passwordHash;

    @Column(value = "enabled")
    private Boolean enabled = true;

    public MarketUser() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }
        MarketUser otherMarketUser = (MarketUser) otherObject;
        return Objects.equals(this.id, otherMarketUser.id)
                && Objects.equals(this.username, otherMarketUser.username)
                && Objects.equals(this.passwordHash, otherMarketUser.passwordHash)
                && Objects.equals(this.enabled, otherMarketUser.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.username, this.passwordHash, this.enabled);
    }

    @Override
    public String toString() {
        return "MarketUser{" +
                "id=" + this.id +
                ", username='" + this.username +
                ", passwordHash='" + this.passwordHash +
                ", enabled=" + this.enabled +
                '}';
    }

}
