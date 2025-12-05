package com.amit.mymarket.unit.item.service;


import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.repository.projection.ItemWithQuantity;
import com.amit.mymarket.item.service.impl.DefaultCatalogQueryService;
import com.amit.mymarket.item.service.type.SortType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class DefaultCatalogQueryServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private DefaultCatalogQueryService catalogQueryService;

    @Test
    @DisplayName(value = "Should return non empty page with items when repository returns results")
    void getCatalogPage_shouldReturnNonEmptyPageWhenRepositoryReturnsResults() {
        String searchQuery = "apple";
        SortType sortType = SortType.ALPHA;
        int pageNumber = 0;
        int pageSize = 10;

        ItemWithQuantity itemWithQuantity = new ItemWithQuantity(
                1L,
                "Apple",
                "Fresh green apple",
                "/images/apple.png",
                100L,
                2
        );

        Item item = new Item();
        item.setId(1L);
        item.setTitle("Apple");
        item.setDescription("Fresh green apple");
        item.setImagePath("/images/apple.png");
        item.setPriceMinor(100L);

        when(this.itemRepository.searchItemsWithQuantity(isNull(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(Flux.just(itemWithQuantity));

        when(this.itemRepository.countItemsBySearchQuery(anyString()))
                .thenReturn(Mono.just(1L));

        when(this.itemRepository.findAllById(anyIterable()))
                .thenReturn(Flux.just(item));

        Mono<Page<Item>> catalogPage = this.catalogQueryService.getCatalogPage(searchQuery, sortType, pageNumber, pageSize);

        StepVerifier.create(catalogPage)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements());
                    assertEquals(1, page.getContent().size());
                    Item firstItem = page.getContent().getFirst();
                    assertEquals(1L, firstItem.getId());
                    assertEquals("Apple", firstItem.getTitle());
                })
                .verifyComplete();

        verify(this.itemRepository, times(1)).searchItemsWithQuantity(isNull(), anyString(), anyString(), anyLong(), anyLong());
        verify(this.itemRepository, times(1)).countItemsBySearchQuery(anyString());
        verify(this.itemRepository, times(1)).findAllById(anyIterable());
    }

    @Test
    @DisplayName(value = "Should return empty page when repository returns no items")
    void getCatalogPage_shouldReturnEmptyPageWhenRepositoryReturnsNoItems() {
        String searchQuery = "unknown";
        SortType sortType = SortType.ALPHA;
        int pageNumber = 0;
        int pageSize = 10;

        when(this.itemRepository.searchItemsWithQuantity(isNull(), anyString(), anyString(), anyLong(), anyLong())).thenReturn(Flux.empty());

        when(this.itemRepository.countItemsBySearchQuery(anyString())).thenReturn(Mono.just(0L));

        Mono<Page<Item>> catalogPage = this.catalogQueryService.getCatalogPage(searchQuery, sortType, pageNumber, pageSize);

        StepVerifier.create(catalogPage)
                .assertNext(page -> {
                    assertEquals(0, page.getTotalElements());
                    assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();

        verify(this.itemRepository, times(1)).searchItemsWithQuantity(isNull(), anyString(), anyString(), anyLong(), anyLong());
        verify(this.itemRepository, times(1)).countItemsBySearchQuery(anyString());
        verify(this.itemRepository, never()).findAllById(anyIterable());
    }

    @Test
    @DisplayName(value = "Should return quantity when item with quantity exists in cart")
    void getCartQuantityForItem_shouldReturnQuantityWhenItemWithQuantityExists() {
        String sessionId = "session-123";
        long itemId = 1L;

        ItemWithQuantity itemWithQuantity = new ItemWithQuantity(
                itemId,
                "Apple",
                "Fresh green apple",
                "/images/apple.png",
                100L,
                5
        );

        when(this.itemRepository.findItemWithQuantity(itemId, sessionId)).thenReturn(Mono.just(itemWithQuantity));

        Mono<Integer> quantity = this.catalogQueryService.getCartQuantityForItem(sessionId, itemId);

        StepVerifier.create(quantity)
                .expectNext(5)
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return zero when quantity is null for item in cart")
    void getCartQuantityForItem_shouldReturnZeroWhenQuantityIsNull() {
        String sessionId = "session-123";
        long itemId = 1L;

        ItemWithQuantity itemWithQuantity = new ItemWithQuantity(
                itemId,
                "Apple",
                "Fresh green apple",
                "/images/apple.png",
                100L,
                null
        );

        when(this.itemRepository.findItemWithQuantity(itemId, sessionId)).thenReturn(Mono.just(itemWithQuantity));

        Mono<Integer> quantity = this.catalogQueryService.getCartQuantityForItem(sessionId, itemId);

        StepVerifier.create(quantity)
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return zero when item is not found in cart for session")
    void getCartQuantityForItem_shouldReturnZeroWhenItemIsNotFound() {
        String sessionId = "session-123";
        long itemId = 1L;

        when(this.itemRepository.findItemWithQuantity(itemId, sessionId)).thenReturn(Mono.empty());

        Mono<Integer> quantity = this.catalogQueryService.getCartQuantityForItem(sessionId, itemId);

        StepVerifier.create(quantity)
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty map when item identifier list is empty")
    void getCartQuantitiesForItems_shouldReturnEmptyMapWhenItemIdentifierListIsEmpty() {
        String sessionIdentifier = "session-123";
        List<Long> itemIdentifierList = Collections.emptyList();

        Mono<Map<Long, Integer>> quantities = this.catalogQueryService.getCartQuantitiesForItems(sessionIdentifier, itemIdentifierList);

        StepVerifier.create(quantities)
                .assertNext(resultMap -> assertTrue(resultMap.isEmpty()))
                .verifyComplete();

        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return quantities for requested item identifiers and default zero for missing ones")
    void getCartQuantitiesForItems_shouldReturnQuantitiesAndZeroForMissingItems() {
        String sessionId = "session-123";
        List<Long> itemId = List.of(1L, 2L, 3L);

        CartItemRow firstCartItemRow = new CartItemRow(
                1L,
                "Apple",
                "Green apple",
                "/images/apple.png",
                100L,
                5
        );

        CartItemRow thirdCartItemRow = new CartItemRow(
                3L,
                "Carrot",
                "Orange carrot",
                "/images/carrot.png",
                75L,
                null
        );

        when(this.cartItemRepository.findCartItems(sessionId)).thenReturn(Flux.just(firstCartItemRow, thirdCartItemRow));

        Mono<Map<Long, Integer>> quantities = this.catalogQueryService.getCartQuantitiesForItems(sessionId, itemId);

        StepVerifier.create(quantities)
                .assertNext(resultMap -> {
                    assertEquals(3, resultMap.size());
                    assertEquals(5, resultMap.get(1L));
                    assertEquals(0, resultMap.get(2L));
                    assertEquals(0, resultMap.get(3L));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return item when item with given identifier exists")
    void getItemById_shouldReturnItemWhenItemExists() {
        long itemId = 10L;

        Item item = new Item();
        item.setId(itemId);
        item.setTitle("Test item");

        when(this.itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mono<Item> itemMono = this.catalogQueryService.getItemById(itemId);

        StepVerifier.create(itemMono)
                .assertNext(foundItem -> {
                    assertEquals(itemId, foundItem.getId());
                    assertEquals("Test item", foundItem.getTitle());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should throw ResourceNotFoundException when item with given identifier does not exist")
    void getItemById_shouldThrowResourceNotFoundExceptionWhenItemDoesNotExist() {
        long itemId = 10L;

        when(this.itemRepository.findById(itemId)).thenReturn(Mono.empty());

        Mono<Item> itemMono = this.catalogQueryService.getItemById(itemId);

        StepVerifier.create(itemMono)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertTrue(throwable.getMessage().contains("Item not found: id=" + itemId));
                })
                .verify();
    }

}
