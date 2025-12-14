package com.amit.storage.util;

import org.apache.tika.Tika;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

public final class MimeTypeResolver {

    private static final Tika TIKA = new Tika();

    public static String resolve(String filename, byte[] content) {
        String mimeType = detectMimeType(content, filename);

        if (!StringUtils.hasText(mimeType)) {
            mimeType = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
        }

        return normalizeMimeType(mimeType);
    }

    private static String detectMimeType(byte[] content, String filename) {
        try {
            return TIKA.detect(content, filename);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String normalizeMimeType(String mimeType) {
        String mime = mimeType.toLowerCase();

        return switch (mime) {
            case "image/jpg", "image/pjpeg" -> "image/jpeg";
            default -> mime;
        };
    }

    private MimeTypeResolver() {
        throw new UnsupportedOperationException();
    }

}
