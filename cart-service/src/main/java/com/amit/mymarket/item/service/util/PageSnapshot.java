package com.amit.mymarket.item.service.util;

import java.util.List;

public record PageSnapshot<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements) {
}
