package com.hand.demo.service;

import java.beans.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hand.demo.model.entity.MediaItem;
import com.hand.demo.repository.MediaRepository;
import com.hand.demo.storage.BlobStorage;
import com.hand.demo.util.FileValidator;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final BlobStorage storage;
    private final MediaRepository mediaRepo;

    public record UploadResponse(UUID id,
            String path, String absoluteUrl, String mime,
            Integer width, Integer height, Long sizeBytes) {
    }

    @Transactional
    public MediaItem uploadImage(Long appUserId, MultipartFile file) throws IOException {
        var meta = FileValidator.validateImageFile(file, 5, 6000, 6000, 30);
        String ext = switch (meta.mime()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IOException("Unsupported MIME: " + meta.mime());
        };

        String datePath = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = java.util.UUID.randomUUID().toString().substring(0, 5)
                + "_" + System.nanoTime() + "." + ext;

        try (InputStream in = file.getInputStream()) {
            var res = storage.save(datePath, fileName, meta.mime(), meta.sizeBytes(), in);

            var item = mediaRepo.findByPublicPath(res.publicPath())
                    .orElseGet(com.hand.demo.model.entity.MediaItem::new);

            if (item.getId() == null) {
                item.setPublicPath(res.publicPath());
                item.setCreatedAt(java.time.Instant.now());
            }
            item.setUserId(appUserId);
            item.setMime(meta.mime());
            item.setWidth(meta.width());
            item.setHeight(meta.height());
            item.setSizeBytes(meta.sizeBytes());
            item.setStatus(com.hand.demo.model.entity.MediaItem.Status.ACTIVE);
            item.setLastUsedAt(java.time.Instant.now());
            item.setAbsoluteUrl(res.absoluteUrl());
            System.out.println( item.toString());
            return mediaRepo.save(item);

        }
    }

    public UploadResponse uploadResponse(com.hand.demo.model.entity.MediaItem img) {
        return new UploadResponse(img.getId(),
                img.getPublicPath(), img.getAbsoluteUrl(),
                img.getMime(), img.getWidth(), img.getHeight(), img.getSizeBytes());

    }

    public MediaItem findMediaById(UUID id) {
        return mediaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("media item not found for id" + id));
    }

    public List<MediaItem> findMediaByUserId(Long userId) {
        return mediaRepo.findByUserId(userId);
    }

    /** يقبل فقط /images/** ويرجع path مُطبّع */
    public String normalizeFromClient(String rawUrlOrPath) {
        return storage.normalizePublicPath(rawUrlOrPath);
    }

    @Transactional
    public void deleteByPublicPath(String rawUrlOrPath) throws IOException {
        String path = storage.normalizePublicPath(rawUrlOrPath);
        // وسم السجل كـ DELETED (بدل الحذف الفوري إن بدك إعادة استخدام لاحقًا)
        mediaRepo.findByPublicPath(path).ifPresent(mi -> {
            mi.setStatus(com.hand.demo.model.entity.MediaItem.Status.DELETED);
            mi.setLastUsedAt(java.time.Instant.now());
            mediaRepo.save(mi);
        });
        // حذف الفيزيائي اختياري حسب سياسة التنظيف لديك
        storage.delete(path);
    }

    @Transient
    public void removeItem( UUID mediaId,Long userId) throws IOException {

        MediaItem mediaItem = mediaRepo.findByIdAndUserId(mediaId,userId)
                .orElseThrow(() -> new EntityNotFoundException("Media item not found for id" + mediaId));

        storage.delete(mediaItem.getPublicPath());


    }

}
