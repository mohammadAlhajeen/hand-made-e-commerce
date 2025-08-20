package com.hand.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "product_images")
public class ProductImage {

  @Id     @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonBackReference
  private Product product;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "mediaItem_id", nullable = false)
  @JsonBackReference
  private MediaItem media;

  @Column(name = "is_main", nullable = false)
  private boolean main;

  @Column(name = "sort_order")
  private Integer sortOrder;

  public ProductImage(Product product, MediaItem media, boolean main, Integer sortOrder) {
    this.product = product;
    this.media = media;
    this.main = main;
    this.sortOrder = sortOrder;
  }
  
}
