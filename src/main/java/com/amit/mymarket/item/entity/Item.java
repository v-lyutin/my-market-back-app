package com.amit.mymarket.item.entity;

import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(schema = "shop", name = "items")
public class Item {

    @Id
    private Long id;

    @Column(value = "title")
    private String title;

    @Column(value = "description")
    private String description;

    @Column(value = "img_path")
    private String imagePath;

    @Min(value = 0)
    @Column(value = "price_minor")
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
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }
        Item otherItem = (Item) otherObject;
        return Objects.equals(this.id, otherItem.id)
                && Objects.equals(this.title, otherItem.title)
                && Objects.equals(this.description, otherItem.description)
                && Objects.equals(this.imagePath, otherItem.imagePath)
                && Objects.equals(this.priceMinor, otherItem.priceMinor);
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
                ", priceMinor=" + this.priceMinor +
                '}';
    }

}
