package com.amit.mymarket.unit.item.util;

import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.service.util.CatalogPageRequest;
import com.amit.mymarket.item.service.util.SearchNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CatalogPageRequestTest {

    @Test
    @DisplayName(value = "Should build request with normalized search and resolved sort type and pageable")
    void of_shouldBuildRequestWithNormalizedSearchAndResolvedSortTypeAndPageable() {
        String rawSearchQuery = "  Apple  ";
        String normalizedSearchQuery = "apple";
        SortType sortType = SortType.ALPHA;
        int pageNumber = 2;
        int pageSize = 20;

        try (var searchNormalizerMock = mockStatic(SearchNormalizer.class)) {
            searchNormalizerMock
                    .when(() -> SearchNormalizer.normalizeSearch(rawSearchQuery))
                    .thenReturn(normalizedSearchQuery);

            CatalogPageRequest catalogPageRequest = CatalogPageRequest.of(rawSearchQuery, sortType, pageNumber, pageSize);

            assertEquals(normalizedSearchQuery, catalogPageRequest.searchQuery());
            assertEquals(SortType.ALPHA.name(), catalogPageRequest.sort());

            Pageable pageable = catalogPageRequest.pageable();
            assertEquals(1, pageable.getPageNumber());
            assertEquals(20, pageable.getPageSize());
            assertEquals(20L, catalogPageRequest.limit());
            assertEquals(20L, catalogPageRequest.offset());
        }
    }

    @Test
    @DisplayName(value = "Should resolve page number and page size to minimum values when they are less than one")
    void of_shouldResolvePageNumberAndPageSizeToMinimumValuesWhenLessThanOne() {
        String searchQuery = null;
        SortType sortType = SortType.ALPHA;
        int pageNumber = 0;
        int pageSize = 0;

        try (var searchNormalizerMock = mockStatic(SearchNormalizer.class)) {
            searchNormalizerMock
                    .when(() -> SearchNormalizer.normalizeSearch(searchQuery))
                    .thenReturn(null);

            CatalogPageRequest catalogPageRequest = CatalogPageRequest.of(searchQuery, sortType, pageNumber, pageSize);

            Pageable pageable = catalogPageRequest.pageable();
            assertEquals(0, pageable.getPageNumber());
            assertEquals(1, pageable.getPageSize());
            assertEquals(1L, catalogPageRequest.limit());
            assertEquals(0L, catalogPageRequest.offset());
        }
    }

    @Test
    @DisplayName(value = "Should use NO sort type when sort type is null")
    void of_shouldUseNoSortTypeWhenSortTypeIsNull() {
        String rawSearchQuery = "phone";
        String normalizedSearchQuery = "phone-normalized";
        SortType sortType = null;
        int pageNumber = 1;
        int pageSize = 10;

        try (var searchNormalizerMock = mockStatic(SearchNormalizer.class)) {
            searchNormalizerMock
                    .when(() -> SearchNormalizer.normalizeSearch(rawSearchQuery))
                    .thenReturn(normalizedSearchQuery);

            CatalogPageRequest catalogPageRequest = CatalogPageRequest.of(rawSearchQuery, sortType, pageNumber, pageSize);

            assertEquals(normalizedSearchQuery, catalogPageRequest.searchQuery());
            assertEquals(SortType.NO.name(), catalogPageRequest.sort());

            Pageable pageable = catalogPageRequest.pageable();
            assertEquals(PageRequest.of(0, 10), pageable);
            assertEquals(10L, catalogPageRequest.limit());
            assertEquals(0L, catalogPageRequest.offset());
        }
    }

}
