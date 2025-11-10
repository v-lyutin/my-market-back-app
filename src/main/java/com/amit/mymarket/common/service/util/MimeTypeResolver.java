package com.amit.mymarket.common.service.util;

import org.apache.tika.Tika;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public final class MimeTypeResolver {

    private static final Tika TIKA = new Tika();

    public static String resolve(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String mime = TIKA.detect(inputStream, file.getOriginalFilename());
            if (mime == null || mime.isBlank()) {
                return file.getContentType() != null ? file.getContentType() : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
            }
            if ("image/jpg".equalsIgnoreCase(mime) || "image/pjpeg".equalsIgnoreCase(mime)) {
                return "image/jpeg";
            }
            return mime.toLowerCase();
        } catch (Exception exception) {
            return file.getContentType() != null ? file.getContentType() : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private MimeTypeResolver() {
        throw new UnsupportedOperationException();
    }

}
