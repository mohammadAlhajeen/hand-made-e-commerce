package com.hand.demo.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

public final class FileValidator {
    private static final Tika TIKA = new Tika();
    private static final Set<String> ALLOWED_MIME = Set.of("image/png","image/jpeg","image/jpg","image/webp");

    public record ImageMeta(String mime, int width, int height, long sizeBytes) {}

    private FileValidator(){}

    public static ImageMeta validateImageFile(MultipartFile file, int maxSizeMB,
                                              int maxWidth, int maxHeight, int maxMegaPixels) throws IOException {
        if (file == null || file.isEmpty()) throw new IOException("File is empty");

        long sizeBytes = file.getSize();
        long maxBytes = (long) maxSizeMB * 1024L * 1024L;
        if (sizeBytes > maxBytes) throw new IOException("File too large: " + sizeBytes);

        String mime;
        try (InputStream in = file.getInputStream()) { mime = TIKA.detect(in); }
        if (!ALLOWED_MIME.contains(mime)) throw new IOException("Unsupported image type: " + mime);

        Dim dim = probeDimensions(file, mime);
        if (dim.w <= 0 || dim.h <= 0) throw new IOException("Cannot determine image dimensions");

        if (maxWidth > 0 && dim.w > maxWidth) throw new IOException("Image width too large: " + dim.w);
        if (maxHeight > 0 && dim.h > maxHeight) throw new IOException("Image height too large: " + dim.h);
        long pixels = (long) dim.w * dim.h;
        if (maxMegaPixels > 0 && pixels > (long) maxMegaPixels * 1_000_000L)
            throw new IOException("Image megapixels too large: " + (pixels / 1_000_000.0) + " MP");

        return new ImageMeta(normalizeMime(mime), dim.w, dim.h, sizeBytes);
    }

    public static void validateImageFile(MultipartFile file, int maxSizeMB) throws IOException {
        validateImageFile(file, maxSizeMB, 6000, 6000, 24);
    }

    // helpers
    private static String normalizeMime(String mime) {
        return Objects.requireNonNull(mime).equalsIgnoreCase("image/jpg") ? "image/jpeg" : mime.toLowerCase();
    }
    private record Dim(int w,int h) {}
    private static Dim probeDimensions(MultipartFile file, String mime) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mime);
            if (!readers.hasNext()) readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader r = readers.next();
                try { r.setInput(iis, true, true); return new Dim(r.getWidth(0), r.getHeight(0)); }
                finally { r.dispose(); }
            }
        }
        try (InputStream in = file.getInputStream()) {
            BufferedImage bi = ImageIO.read(in);
            if (bi != null) return new Dim(bi.getWidth(), bi.getHeight());
        }
        return new Dim(-1,-1);
    }
}
