package com.amit.storage.util;

import java.util.List;

public final class MediaFileValidator {

    public static void validateFileSize(long size, long maxBytes) {
        if (size < 0) {
            return;
        }
        if (maxBytes > 0 && size > maxBytes) {
            throw new IllegalArgumentException("File exceeds size limit: " + size);
        }
    }

    public static void validateMimeType(String mime, List<String> allowed) {
        if (mime == null || mime.isBlank()) {
            throw new IllegalArgumentException("Cannot detect MIME type");
        }
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        if (!allowed.contains(mime)) {
            throw new IllegalArgumentException("MIME type not allowed: " + mime);
        }
    }

    private MediaFileValidator() {
        throw new UnsupportedOperationException();
    }

}
