package com.amit.mymarket.item.service.util;

import com.amit.mymarket.item.service.type.SortType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record CatalogPageRequest(
        String searchQuery,
        String sort,
        Pageable pageable) {

    public static CatalogPageRequest of(String searchQuery, SortType sortType, int pageNumber, int pageSize) {
        return new CatalogPageRequest(
                normalizeSearch(searchQuery),
                resolveSortType(sortType),
                createPageable(pageNumber, pageSize)
        );
    }

    public long limit() {
        return this.pageable.getPageSize();
    }

    public long offset() {
        return (long) this.pageable.getPageNumber() * this.pageable.getPageSize();
    }

    private static Pageable createPageable(int pageNumber, int pageSize) {
        return PageRequest.of(
                resolvePageNumber(pageNumber),
                resolvePageSize(pageSize)
        );
    }

    private static int resolvePageNumber(int pageNumber) {
        return Math.max(pageNumber - 1, 0);
    }

    private static int resolvePageSize(int pageSize) {
        return Math.max(pageSize, 1);
    }

    private static String normalizeSearch(String searchQuery) {
        return SearchNormalizer.normalizeSearch(searchQuery);
    }

    private static String resolveSortType(SortType sortType) {
        return sortType != null ? sortType.name() : SortType.NO.name();
    }

}
