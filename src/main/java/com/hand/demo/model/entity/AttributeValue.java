package com.hand.demo.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(
    name = "attribute_values",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attribute_value_unique",
        columnNames = {"attribute_id", "value"}
    )
)
public class AttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ملاحظة: إن كان النوع NUMBER، خزّنه نصيًا هنا وتحقق بالكود */
    @NotBlank
    @Column(nullable = false, length = 255)
    private String value;

    @Column(name = "extra_price", precision = 10, scale = 2)
    private BigDecimal extraPrice; // اختياري: زيادة سعرية للـ option

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id")
    @JsonBackReference
    private Attribute attribute;

    @OneToMany(mappedBy = "attributeValue", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AttributeValueImage> attributeValueImages = new ArrayList<>();

    public void addImage(AttributeValueImage img) {
        if (attributeValueImages == null) attributeValueImages = new ArrayList<>();
        img.setAttributeValue(this);
        attributeValueImages.add(img);
    }
}
