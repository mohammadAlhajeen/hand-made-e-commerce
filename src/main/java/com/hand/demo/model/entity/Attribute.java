package com.hand.demo.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(
    name = "attributes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_product_attribute_name",
        columnNames = {"product_id", "name"}
    )
)
public class Attribute {

    public enum AttributeType { TEXT, NUMBER, SELECT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 16)
    private AttributeType type = AttributeType.TEXT;

    @NotNull
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AttributeValue> attributeValues = new ArrayList<>(5);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** استخدم “mutate” بدل الاستبدال للحفاظ على orphanRemoval */
    public void setAttributeValues(List<AttributeValue> newValues) {
        if (this.attributeValues == null) {
            this.attributeValues = new ArrayList<>();
        }
        this.attributeValues.clear();
        if (newValues != null) {
            for (AttributeValue v : newValues) {
                v.setAttribute(this);
                this.attributeValues.add(v);
            }
        }
    }

    /** إضافة عنصر واحد بشكل آمن */
    public void addValue(AttributeValue val) {
        if (this.attributeValues == null) this.attributeValues = new ArrayList<>();
        val.setAttribute(this);
        this.attributeValues.add(val);
    }
}
