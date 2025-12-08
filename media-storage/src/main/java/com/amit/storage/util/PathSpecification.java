package com.amit.storage.util;

public record PathSpecification(
        String namespace,
        String ownerId,
        String variant) {

    public static PathSpecification of(String namespace, String ownerId, String variant) {
        return new PathSpecification(
                requireNonBlank(namespace, "namespace"),
                ownerId == null ? "" : ownerId.trim(),
                requireNonBlank(variant, "variant")
        );
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is blank");
        }
        return value.trim();
    }

}
