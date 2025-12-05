package com.amit.mymarket.item.usecase.util;

import java.util.ArrayList;
import java.util.List;

public final class PagingUtils {

    public static <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }

    private PagingUtils() {
        throw new UnsupportedOperationException();
    }

}
