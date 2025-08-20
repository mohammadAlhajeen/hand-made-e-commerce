package com.hand.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "app_user_images")
@Data
@Builder
public class AppUserImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
        @JsonBackReference
    
    private AppUser appUser;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    @JsonBackReference
    private MediaItem mediaItem;
    @Transient
    public String getUrl() {
        return mediaItem != null ? mediaItem.getAbsoluteUrl() : null;
    }
}
