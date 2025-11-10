package com.amit.mymarket.common.service.util;

public final class FilenameSanitizer {

    public static String sanitize(String filename) {
        if (filename == null) {
            return "";
        }
        return filename.replaceAll("[\\r\\n\\t\\\\/]+", "_");
    }

    private FilenameSanitizer() {
        throw new UnsupportedOperationException();
    }

}
