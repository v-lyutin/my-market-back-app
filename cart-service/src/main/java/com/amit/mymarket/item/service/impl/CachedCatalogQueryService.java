package com.amit.mymarket.item.service.impl;

import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.service.CatalogQueryService;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.service.util.PageSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Primary
@Service
public class CachedCatalogQueryService implements CatalogQueryService {

    private static final Duration ITEM_TTL = Duration.ofMinutes(10);

    private static final Duration CATALOG_PAGE_TTL = Duration.ofMinutes(5);

    private final CatalogQueryService delegate;

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CachedCatalogQueryService(@Qualifier("defaultCatalogQueryService") CatalogQueryService delegate,
                                     ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Page<Item>> getCatalogPage(String searchQuery, SortType sortType, int pageNumber, int pageSize) {
        String cacheKey = buildCatalogPageKey(searchQuery, sortType, pageNumber, pageSize);
        return this.redisTemplate.opsForValue()
                .get(cacheKey)
                .cast(PageSnapshot.class)
                .map(snapshotObj -> {
                    @SuppressWarnings("unchecked")
                    PageSnapshot<Item> snapshot = (PageSnapshot<Item>) snapshotObj;

                    Pageable pageable = PageRequest.of(
                            snapshot.pageNumber(),
                            snapshot.pageSize()
                    );

                    return (Page<Item>) new PageImpl<>(
                            snapshot.content(),
                            pageable,
                            snapshot.totalElements()
                    );
                })
                .switchIfEmpty(Mono.defer(() ->
                        this.delegate.getCatalogPage(searchQuery, sortType, pageNumber, pageSize)
                                .flatMap(page -> {
                                    PageSnapshot<Item> snapshot = new PageSnapshot<>(
                                            page.getContent(),
                                            page.getNumber(),
                                            page.getSize(),
                                            page.getTotalElements()
                                    );
                                    return this.redisTemplate.opsForValue()
                                            .set(cacheKey, snapshot, CATALOG_PAGE_TTL)
                                            .thenReturn(page);
                                })
                ));
    }

    @Override
    public Mono<Integer> getCartQuantityForItem(String sessionId, long itemId) {
        return this.delegate.getCartQuantityForItem(sessionId, itemId);
    }

    @Override
    public Mono<Map<Long, Integer>> getCartQuantitiesForItems(String sessionId, List<Long> itemIds) {
        return this.delegate.getCartQuantitiesForItems(sessionId, itemIds);
    }

    @Override
    public Mono<Item> getItemById(long itemId) {
        String cacheKey = buildItemKey(itemId);
        return this.redisTemplate.opsForValue()
                .get(cacheKey)
                .cast(Item.class)
                .switchIfEmpty(Mono.defer(() ->
                        this.delegate.getItemById(itemId)
                                .flatMap(item ->
                                        this.redisTemplate.opsForValue()
                                                .set(cacheKey, item, ITEM_TTL)
                                                .thenReturn(item)
                                )
                ));
    }

    private static String buildCatalogPageKey(String searchQuery,
                                              SortType sortType,
                                              int pageNumber,
                                              int pageSize) {
        String normalizedSearch = (searchQuery == null || searchQuery.isBlank())
                ? "_"
                : searchQuery.trim().toLowerCase();

        return "catalog:page:" +
                "q=" + normalizedSearch +
                ":sort=" + sortType.name() +
                ":page=" + pageNumber +
                ":size=" + pageSize;
    }

    private static String buildItemKey(long itemId) {
        return "catalog:item:" + itemId;
    }

}
