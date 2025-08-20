package com.hand.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "media_items")
@Data
public class MediaItem {

    public enum Status {
        ACTIVE, DELETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 512, unique = true)
    private String publicPath; // /images/2025/08/abc.jpg
    private String absoluteUrl; //  https://example.com/images/2025/08/abc.jpg
    @OneToOne(mappedBy = "mediaItem", fetch = FetchType.LAZY)
    @JsonBackReference
    private AppUserImage appUser;
    @OneToMany(mappedBy = "mediaItem", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<AttributeValueImage> attValImg;
    @OneToMany(mappedBy = "media", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<ProductImage> productImage;
    @Column(nullable = false, length = 64)
    private String mime;
    private Long userId;

    private Integer width;
    private Integer height;
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, name = "status")
    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant lastUsedAt;
}
