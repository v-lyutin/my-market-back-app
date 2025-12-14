package com.amit.storage.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MimeTypeResolverTest {

    @Test
    @DisplayName("Should detect image/png for real PNG file")
    void resolve_shouldDetectPngFile() throws IOException {
        try (InputStream fileInputStream = getClass().getResourceAsStream("/test-files/sample.png")) {
            assertNotNull(fileInputStream, "sample.png not found in resources");

            byte[] fileContent = fileInputStream.readAllBytes();
            String resolvedMimeType = MimeTypeResolver.resolve("sample.png", fileContent);

            assertEquals("image/png", resolvedMimeType);
        }
    }

    @Test
    @DisplayName("Should detect image/jpeg for real JPEG file")
    void resolve_shouldDetectJpegFile() throws IOException {
        try (InputStream fileInputStream = getClass().getResourceAsStream("/test-files/sample.jpg")) {
            assertNotNull(fileInputStream, "sample.jpg not found in resources");

            byte[] fileContent = fileInputStream.readAllBytes();
            String resolvedMimeType = MimeTypeResolver.resolve("sample.jpg", fileContent);

            assertEquals("image/jpeg", resolvedMimeType);
        }
    }

    @Test
    @DisplayName("Should return application/octet-stream for empty file content")
    void resolve_shouldReturnOctetStreamForEmptyFileContent() {
        String filename = "empty.bin";
        byte[] fileContent = new byte[0];

        String resolvedMimeType = MimeTypeResolver.resolve(filename, fileContent);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, resolvedMimeType);
    }

    @Test
    @DisplayName("Should return application/octet-stream for unknown binary content")
    void resolve_shouldReturnOctetStreamForUnknownBinaryContent() {
        String filename = "unknown.bin";
        byte[] fileContent = new byte[] { 1, 2, 3, 4, 5 };

        String resolvedMimeType = MimeTypeResolver.resolve(filename, fileContent);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, resolvedMimeType);
    }

}
