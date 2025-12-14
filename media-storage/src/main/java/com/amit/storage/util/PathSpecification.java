package com.amit.storage.util;

import org.springframework.util.StringUtils;

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
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " is blank");
        }
        return value.trim();
    }

}
