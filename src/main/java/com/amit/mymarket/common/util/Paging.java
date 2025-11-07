package com.amit.mymarket.common.util;

public record Paging(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext) {
}
