package com.hand.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.MediaItem;
import com.hand.demo.repository.MediaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaLookupService {
  private final MediaRepository mediaRepo;

  @Value("${app.images.public-base:/images/}")
  private String publicBase;

  public MediaItem resolveExistingOrThrow(String rawUrl) {
    String path = canonicalize(rawUrl);
    return mediaRepo.findByPublicPath(path)
        .filter(m -> m.getStatus() == MediaItem.Status.ACTIVE)
        .orElseThrow(() -> new IllegalArgumentException("Image not found in media library: " + path));
  }

  private String canonicalize(String url) {
    String u = (url == null ? "" : url.trim());
    if (u.startsWith("http://") || u.startsWith("https://")) {
      u = java.net.URI.create(u).getPath();
    }
    if (!u.startsWith(publicBase)) throw new IllegalArgumentException("Only local " + publicBase + "* allowed");
    if (u.contains("..")) throw new SecurityException("Invalid path");
    if (!u.toLowerCase().matches("^/images/[\\w\\-./%]+\\.(png|jpe?g|webp|gif)$"))
      throw new IllegalArgumentException("Unsupported image path");
    return u.replaceAll("/+", "/");
  }
}
