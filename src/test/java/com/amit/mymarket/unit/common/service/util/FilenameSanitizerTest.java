package com.amit.mymarket.unit.common.service.util;

import com.amit.mymarket.common.service.util.FilenameSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

class FilenameSanitizerTest {

    @Test
    @DisplayName(value = "Should return empty string when filename is null")
    void sanitize_shouldReturnEmptyStringWhenFilenameIsNull() {
        assertEquals("", FilenameSanitizer.sanitize(null));
    }

    @Test
    @DisplayName(value = "Should return empty string when filename is empty")
    void sanitize_shouldReturnEmptyStringWhenFilenameIsEmpty() {
        assertEquals("", FilenameSanitizer.sanitize(""));
    }

    @ParameterizedTest(name = "Should sanitize \"{0}\" -> \"{1}\"")
    @DisplayName(value = "Should replace slashes and backslashes with single underscore")
    @CsvSource(value = {
            // Single forbidden characters
            "file/name.jpg|file_name.jpg",
            "file\\\\name.jpg|file_name.jpg",

            // Multiple forbidden characters collapse into one underscore
            "file///name.jpg|file_name.jpg",
            "file\\\\\\\\\\\\name.jpg|file_name.jpg",

            // Mixed forbidden characters at edges
            "/file/name/.jpg|_file_name_.jpg",
            "\\\\\\\\file//name\\\\|_file_name_",

            // Safe names remain unchanged
            "my_file-01.png|my_file-01.png",

            // Unicode characters remain untouched
            "товар/изображение.png|товар_изображение.png",
            "Фото\\\\лето 2025.webp|Фото_лето 2025.webp",

            // No extension
            "unsafe/name|unsafe_name"
    }, delimiter = '|')
    void shouldReplaceSlashesAndBackslashes(String input, String expected) {
        assertEquals(expected, FilenameSanitizer.sanitize(input));
    }

    @ParameterizedTest(name = "Should sanitize with control chars: \"{0}\" -> \"{1}\"")
    @DisplayName(value = "Should collapse CR/LF/TAB sequences into single underscore")
    @MethodSource(value = "controlCharCases")
    void shouldHandleControlCharacters(String input, String expected) {
        assertEquals(expected, FilenameSanitizer.sanitize(input));
    }

    static Stream<Arguments> controlCharCases() {
        return Stream.of(
                of("file\tname.jpg", "file_name.jpg"),
                of("file\nname.jpg", "file_name.jpg"),
                of("file\rname.jpg", "file_name.jpg"),
                of("file\r\n\t/name.jpg", "file_name.jpg"),
                of("\\\\\\///\t\n", "_")
        );
    }

    @Test
    @DisplayName(value = "Should preserve existing underscores")
    void sanitize_shouldPreserveExistingUnderscores() {
        assertEquals("my__file.png", FilenameSanitizer.sanitize("my__file.png"));
    }

}
