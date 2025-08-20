package com.hand.demo.storage;

import java.io.IOException;
import java.io.InputStream;

public interface BlobStorage {

    record StoreResult(String publicPath, String absoluteUrl, String etag) {}

    StoreResult save(String preferredDir, String fileNameWithExt, String contentType,
                     long contentLength, InputStream in) throws IOException;

    void delete(String publicPath) throws IOException;

    boolean exists(String publicPath) throws IOException;

    String normalizePublicPath(String rawUrlOrPath);

    String toAbsoluteUrl(String publicPath);
}
