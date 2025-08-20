package com.hand.demo.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class LocalBlobStorage implements BlobStorage {

    private final Path root;
    private final String publicBase;
    private final String appBaseUrl;

    public LocalBlobStorage(String storageDir, String publicBase, String appBaseUrl) throws IOException {
        this.root = Paths.get(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
        this.publicBase = ensureBase(publicBase);
        this.appBaseUrl = appBaseUrl == null ? "" : stripTrailingSlash(appBaseUrl.trim());
    }

    @Override
    public StoreResult save(String preferredDir, String fileNameWithExt, String contentType,
                            long contentLength, InputStream in) throws IOException {

        String subdir = (StringUtils.hasText(preferredDir) ? preferredDir : todayPath());
        Path dir = root.resolve(subdir).normalize();
        Files.createDirectories(dir);

        Path target = dir.resolve(fileNameWithExt).normalize();
        if (!target.startsWith(root)) throw new SecurityException("Path traversal");
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);

        String publicPath = normalizePublicPath(publicBase + subdir + "/" + fileNameWithExt);
        return new StoreResult(publicPath, toAbsoluteUrl(publicPath), null);
    }

    @Override
    public void delete(String publicPath) throws IOException {
        Files.deleteIfExists(toFile(publicPath));
    }

    @Override
    public boolean exists(String publicPath) throws IOException {
        return Files.exists(toFile(publicPath));
    }

    @Override
    public String normalizePublicPath(String raw) {
        String u = (raw == null ? "" : raw.trim());
        if (u.startsWith("http://") || u.startsWith("https://")) {
            u = java.net.URI.create(u).getPath();
        }
        if (!u.startsWith(publicBase)) throw new IllegalArgumentException("Only " + publicBase + "* allowed");
        if (u.contains("..")) throw new SecurityException("Invalid image path");

        String pattern = "^" + Pattern.quote(publicBase) + "[\\w\\-./%]+\\.(png|jpe?g|webp|gif)$";
        if (!u.toLowerCase().matches(pattern)) {
            throw new IllegalArgumentException("Unsupported image path");
        }
        return u.replaceAll("/+", "/");
    }

    @Override
    public String toAbsoluteUrl(String publicPath) {
        return appBaseUrl.isBlank() ? publicPath : appBaseUrl + publicPath;
    }

    // helpers
    private Path toFile(String publicPath) {
        String path = normalizePublicPath(publicPath);
        String rel = path.substring(publicBase.length());
        Path f = root.resolve(rel).normalize();
        if (!f.startsWith(root)) throw new SecurityException("Traversal");
        return f;
    }
    private static String ensureBase(String s) {
        String t = (s == null || s.isBlank()) ? "/images/" : s.trim();
        if (!t.startsWith("/")) t = "/" + t;
        if (!t.endsWith("/"))  t = t + "/";
        return t;
    }
    private static String stripTrailingSlash(String s) {
        while (s.endsWith("/")) s = s.substring(0, s.length()-1);
        return s;
    }
    private static String todayPath() {
        return java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
    }
}
