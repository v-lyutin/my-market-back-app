package com.amit.mymarket.unit.common.service.util;

import com.amit.mymarket.common.service.util.MimeTypeResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MimeTypeResolverTest {

    @Test
    @DisplayName(value = "Should detect image/png for real PNG file")
    void resolve_shouldDetectPngFile() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/test-files/sample.png")) {
            assertNotNull(input, "sample.png not found in resources");

            MockMultipartFile file = new MockMultipartFile("file", "sample.png", null, input);
            String resolvedMimeType = MimeTypeResolver.resolve(file);

            assertEquals("image/png", resolvedMimeType);
        }
    }

    @Test
    @DisplayName(value = "Should detect image/jpeg for real JPEG file")
    void resolve_shouldDetectJpegFile() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/test-files/sample.jpg")) {
            assertNotNull(input, "sample.jpg not found in resources");

            MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", null, input);
            String resolvedMimeType = MimeTypeResolver.resolve(file);

            assertEquals("image/jpeg", resolvedMimeType);
        }
    }

    @Test
    @DisplayName(value = "Should return application/octet-stream for empty file")
    void resolve_shouldReturnOctetStreamForEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.bin", null, new byte[0]);

        String resolvedMimeType = MimeTypeResolver.resolve(file);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, resolvedMimeType);
    }

    @Test
    @DisplayName(value = "Should fallback to file.getContentType() when InputStream throws")
    void resolve_shouldFallbackToContentTypeWhenStreamThrows() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("broken stream"));
        when(file.getOriginalFilename()).thenReturn("whatever.bin");
        when(file.getContentType()).thenReturn("image/gif");

        String resolvedMimeType = MimeTypeResolver.resolve(file);

        assertEquals("image/gif", resolvedMimeType);
    }

    @Test
    @DisplayName(value = "Should fallback to application/octet-stream when InputStream throws and contentType is null")
    void resolve_shouldFallbackToOctetStreamWhenStreamFailsAndNoContentType() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("broken stream"));
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getContentType()).thenReturn(null);

        String resolvedMimeType = MimeTypeResolver.resolve(file);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, resolvedMimeType);
    }

}
