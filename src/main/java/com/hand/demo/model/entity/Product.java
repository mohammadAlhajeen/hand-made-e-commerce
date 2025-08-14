package com.hand.demo.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "products")
@SQLDelete(sql = "UPDATE products SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted = false")

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.IN_STOCK;

    @Column(name = "preparation_days", nullable = true)
    private Integer preparationDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Computed by DB triggers; mapped for read/write convenience
    @Column(name = "average_rating")
    private Double averageRating;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonBackReference
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "products")
    private List<Category> categories;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "products")
    private List<Cart> carts;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private AvgRating avgRating;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attribute> attributes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums******
    public enum AvailabilityStatus {
        IN_STOCK,
        MADE_TO_ORDER
    }

    // Pre data base
    @PrePersist
    protected void onCreate() {
        avgRating = new AvgRating(this);
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Methods
    public void setImages(List<ProductImage> images) {
        this.images = images;
        if (images != null) {
            images.forEach(img -> img.setProduct(this));
        }
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
        if (attributes != null) {
            attributes.forEach(attr -> attr.setProduct(this));
        }
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        review.setProduct(this);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setProduct(this);
    }
}
