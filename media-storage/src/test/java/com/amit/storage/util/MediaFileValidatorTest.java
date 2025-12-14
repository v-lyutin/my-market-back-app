package com.amit.storage.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaFileValidatorTest {

    @ParameterizedTest(name = "Should accept size={0} with maxBytes={1}")
    @DisplayName(value = "Should not throw when size fits the limit or no limit is applied")
    @CsvSource({
            "-1, 0",
            "-1, 100",
            "0, 0",
            "10, 0",
            "10, 10",
            "10, 11",
            "" + Long.MAX_VALUE + ", 0"
    })
    void validateFileSize_shouldNotThrowWhenSizeIsValid(long size, long maxBytes) {
        assertDoesNotThrow(() -> MediaFileValidator.validateFileSize(size, maxBytes));
    }

    @ParameterizedTest(name = "Should reject size={0} with maxBytes={1}")
    @DisplayName(value = "Should throw when size exceeds the limit")
    @CsvSource({
            "11, 10",
            "1001, 1000",
            "" + Long.MAX_VALUE + ", 1"
    })
    void validateFileSize_shouldThrowWhenSizeExceedsLimit(long size, long maxBytes) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> MediaFileValidator.validateFileSize(size, maxBytes));
        assertTrue(exception.getMessage().toLowerCase().contains("size"), "Should mention size in error message");
    }

    @Test
    @DisplayName(value = "Should not throw when allowed list is null or empty (no restriction)")
    void validateMimeType_shouldAcceptWhenAllowedListIsNullOrEmpty() {
        assertDoesNotThrow(() -> MediaFileValidator.validateMimeType("image/png", null));
        assertDoesNotThrow(() -> MediaFileValidator.validateMimeType("image/png", List.of()));
    }

    @Test
    @DisplayName(value = "Should not throw when MIME is in the allowed list")
    void validateMimeType_shouldAcceptWhenMimeIsAllowed() {
        List<String> allowedMimeTypes = List.of("image/jpeg", "image/png", "image/webp");
        assertDoesNotThrow(() -> MediaFileValidator.validateMimeType("image/png", allowedMimeTypes));
    }

    @Test
    @DisplayName(value = "Should throw when MIME is null or blank")
    void validateMimeType_shouldRejectWhenMimeIsNullOrBlank() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> MediaFileValidator.validateMimeType(null, List.of("image/png")));
        assertTrue(exception1.getMessage().toLowerCase().contains("mime"));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> MediaFileValidator.validateMimeType("  ", List.of("image/png")));
        assertTrue(exception2.getMessage().toLowerCase().contains("mime"));
    }

    @Test
    @DisplayName(value = "Should throw when MIME is not in the allowed list")
    void validateMimeType_shouldRejectWhenMimeIsNotAllowed() {
        List<String> allowedMimeTypes = List.of("image/jpeg", "image/png");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> MediaFileValidator.validateMimeType("image/gif", allowedMimeTypes));
        assertTrue(exception.getMessage().toLowerCase().contains("not allowed"));
    }

    @Test
    @DisplayName(value = "Should be case-sensitive by default (exact match required)")
    void validateMimeType_shouldBeCaseSensitiveByDefault() {
        List<String> allowedMimeTypes = List.of("image/png");
        assertThrows(IllegalArgumentException.class, () -> MediaFileValidator.validateMimeType("IMAGE/PNG", allowedMimeTypes));
    }

}
