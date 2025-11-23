package com.amit.mymarket.common.service.util;

import org.apache.tika.Tika;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public final class MimeTypeResolver {

    private static final Tika TIKA = new Tika();

    public static String resolve(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String mimeType = TIKA.detect(inputStream, file.getOriginalFilename());
            if (mimeType == null || mimeType.isBlank()) {
                return file.getContentType() != null ? file.getContentType() : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
            }
            if ("image/jpg".equalsIgnoreCase(mimeType) || "image/pjpeg".equalsIgnoreCase(mimeType)) {
                return "image/jpeg";
            }
            return mimeType.toLowerCase();
        } catch (Exception exception) {
            return file.getContentType() != null ? file.getContentType() : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private MimeTypeResolver() {
        throw new UnsupportedOperationException();
    }

}
