/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hand.demo.model.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "attribute_value_images")
public class AttributeValueImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    @JsonBackReference
    private AttributeValue attributeValue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mediaItem_id", nullable = false,unique = false)
    @JsonBackReference
    private MediaItem mediaItem;

    @jakarta.persistence.Transient
    public String getUrl() {
        return mediaItem != null ? mediaItem.getPublicPath() : null;
    }

}
