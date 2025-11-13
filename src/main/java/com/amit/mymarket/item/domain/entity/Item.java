package com.amit.mymarket.item.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.Hibernate;

@Entity
@Table(schema = "shop", name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, length = 2048)
    private String description;

    @Column(name = "img_path", length = 512)
    private String imagePath;

    @Min(value = 0)
    @Column(name = "price_minor", nullable = false)
    private Long priceMinor;

    public Item() {}

    public Item(String title, String description, Long priceMinor) {
        this.title = title;
        this.description = description;
        this.priceMinor = priceMinor;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getPriceMinor() {
        return this.priceMinor;
    }

    public void setPriceMinor(Long priceMinor) {
        this.priceMinor = priceMinor;
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
        Item otherItem = (Item) otherObject;
        return this.id != null && this.id.equals(otherItem.id);
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + this.id +
                ", title='" + this.title +
                ", description='" + (this.description != null ? this.description.substring(0, Math.min(this.description.length(), 50)) + "..." : null) +
                ", imagePath='" + this.imagePath +
                ", formatPrice=" + this.priceMinor +
                '}';
    }

}
