package com.hand.demo.storage;

import java.io.InputStream;
import java.net.URI;
import java.util.regex.Pattern;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3BlobStorage implements BlobStorage {

    private final S3Client s3;
    private final String bucket;
    private final String prefix;      // "images/"
    private final String publicBase;  // "/images/"
    private final String cdnBaseUrl;  // "https://cdn.example.com" (اختياري)

    public S3BlobStorage(String bucket, String region, String prefix, String publicBase, String cdnBaseUrl) {
        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.bucket = bucket;
        this.prefix = ensurePrefix(prefix);
        this.publicBase = ensurePublicBase(publicBase);
        this.cdnBaseUrl = (cdnBaseUrl == null) ? "" : stripTrailingSlash(cdnBaseUrl.trim());
    }

    @Override
    public StoreResult save(String preferredDir, String fileNameWithExt, String contentType,
                            long contentLength, InputStream in) {

        String subdir = (preferredDir == null || preferredDir.isBlank())
                ? todayPath()
                : preferredDir.replaceAll("^/+", "").replaceAll("/+$", "");

        String publicPath = normalizePublicPath(publicBase + subdir + "/" + fileNameWithExt);
        String key = toKey(publicPath);

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3.putObject(req, RequestBody.fromInputStream(in, contentLength));

        String etag = s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build()).eTag();
        return new StoreResult(publicPath, toAbsoluteUrl(publicPath), etag);
    }

    @Override
    public void delete(String publicPath) {
        String key = toKey(normalizePublicPath(publicPath));
        try { s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build()); }
        catch (S3Exception ignored) {}
    }

    @Override
    public boolean exists(String publicPath) {
        String key = toKey(normalizePublicPath(publicPath));
        try { s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build()); return true; }
        catch (S3Exception e) { return false; }
    }

    @Override
    public String normalizePublicPath(String raw) {
        String u = (raw == null ? "" : raw.trim());
        if (u.startsWith("http://") || u.startsWith("https://")) {
            u = URI.create(u).getPath();
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
        String path = normalizePublicPath(publicPath);
        if (!cdnBaseUrl.isBlank()) return cdnBaseUrl + path; // CloudFront/CDN
        return "https://" + bucket + ".s3.amazonaws.com/" + toKey(path); // بديل بسيط
    }

    // helpers
    private String toKey(String publicPath) {
        String rel = publicPath.substring(publicBase.length()); // yyyy/MM/file.ext
        return prefix + rel;
    }
    private static String ensurePrefix(String p) {
        if (p == null || p.isBlank()) return "images/";
        p = p.replaceAll("^/+", "").replaceAll("/+$", "") + "/";
        return p;
    }
    private static String ensurePublicBase(String s) {
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
