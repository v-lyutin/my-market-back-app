package com.amit.mymarket.item.api.dto;

public record Paging(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext) {
}
