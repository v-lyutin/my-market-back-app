package com.amit.mymarket.item.service.util;

public final class SearchNormalizer {

    public static String normalizeSearch(String searchRow) {
        return (searchRow == null || searchRow.isBlank()) ? null : searchRow.trim();
    }

}
