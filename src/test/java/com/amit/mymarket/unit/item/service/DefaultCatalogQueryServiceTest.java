package com.amit.mymarket.unit.item.service;


import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.repository.projection.ItemWithQuantity;
import com.amit.mymarket.item.service.impl.DefaultCatalogQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
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
    @DisplayName(value = "Should return empty page when repository projection is empty")
    void fetchCatalogPage_shouldReturnEmptyPageWhenRepositoryProjectionIsEmpty() {
        Pageable expectedPageable = PageRequest.of(0, 1);
        when(this.itemRepository.findCatalogWithCounts(isNull(), anyString(), eq(SortType.ALPHA.name()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), expectedPageable, 0));

        Page<Item> resultPage = this.catalogQueryService.fetchCatalogPage("Apple", SortType.ALPHA, 1, 1);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).isEmpty();
        assertThat(resultPage.getTotalElements()).isZero();

        verify(itemRepository, never()).findAllById(anyCollection());
    }

    @Test
    @DisplayName(value = "Should return zero when item-with-quantity projection is null")
    void fetchCartQuantityForItem_shouldReturnZeroWhenItemWithCountProjectionIsNull() {
        when(this.itemRepository.findItemWithCount(100L, "session-1")).thenReturn(null);

        int itemQuantity = this.catalogQueryService.fetchCartQuantityForItem("session-1", 100L);

        assertThat(itemQuantity).isZero();
    }

    @Test
    @DisplayName(value = "Should return zero when item-with-quantity projection has null quantity")
    void fetchCartQuantityForItem_shouldReturnZeroWhenItemWithCountProjectionHasNullQuantity() {
        ItemWithQuantity itemWithCountRow = mock(ItemWithQuantity.class);
        when(itemWithCountRow.getCount()).thenReturn(null);
        when(this.itemRepository.findItemWithCount(200L, "session-2")).thenReturn(itemWithCountRow);

        int itemQuantity = this.catalogQueryService.fetchCartQuantityForItem("session-2", 200L);

        assertThat(itemQuantity).isZero();
    }

    @Test
    @DisplayName(value = "Should return quantity from item-with-quantity projection when present")
    void findItemWithCount_shouldReturnQuantityFromItemWithCountProjectionWhenPresent() {
        ItemWithQuantity itemWithCountRow = mock(ItemWithQuantity.class);
        when(itemWithCountRow.getCount()).thenReturn(5);
        when(this.itemRepository.findItemWithCount(300L, "session-3")).thenReturn(itemWithCountRow);

        int itemQuantity = this.catalogQueryService.fetchCartQuantityForItem("session-3", 300L);

        assertThat(itemQuantity).isEqualTo(5);
    }


    @Test
    @DisplayName(value = "Should return empty map when requested item ids list is null")
    void fetchCartQuantitiesForItems_shouldReturnEmptyMapWhenRequestedItemIdsListIsNull() {
        Map<Long, Integer> result = this.catalogQueryService.fetchCartQuantitiesForItems("session-A", null);
        assertThat(result).isEmpty();
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return empty map when requested item ids list is empty")
    void fetchCartQuantitiesForItems_shouldReturnEmptyMapWhenRequestedItemIdsListIsEmpty() {
        Map<Long, Integer> result = this.catalogQueryService.fetchCartQuantitiesForItems("session-A", Collections.emptyList());
        assertThat(result).isEmpty();
        verifyNoInteractions(this.cartItemRepository);
    }

    @Test
    @DisplayName(value = "Should return quantities for requested ids and zeros for missing or null counts")
    void fetchCartQuantitiesForItems_shouldReturnQuantitiesForRequestedIdsAndZerosForMissingOrNullCounts() {
        List<Long> requestedItemIds = List.of(1L, 2L, 3L);

        CartItemRow row1 = cartItemRow(1L, 2);
        CartItemRow row3 = cartItemRow(3L, null);
        CartItemRow row4 = cartItemRow(4L, 7);

        when(cartItemRepository.findCartItems("session-X")).thenReturn(List.of(row1, row3, row4));

        Map<Long, Integer> result = this.catalogQueryService.fetchCartQuantitiesForItems("session-X", requestedItemIds);

        assertThat(result).containsOnly(
                entry(1L, 2),
                entry(2L, 0),
                entry(3L, 0)
        );
    }

    @Test
    @DisplayName(value = "Should return item when item exists by id")
    void fetchItemOrThrow_shouldReturnItemWhenItemExistsById() {
        Item existingItem = item(777L);
        when(this.itemRepository.findById(777L)).thenReturn(Optional.of(existingItem));

        Item resultItem = this.catalogQueryService.fetchItemOrThrow(777L);

        assertThat(resultItem).isSameAs(existingItem);
    }

    @Test
    @DisplayName(value = "Should throw ResourceNotFoundException when item does not exist by id")
    void fetchItemOrThrow_shouldThrowResourceNotFoundExceptionWhenItemDoesNotExistById() {
        when(this.itemRepository.findById(888L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.catalogQueryService.fetchItemOrThrow(888L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("id=888");
    }

    private static Item item(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setTitle("T-" + id);
        item.setDescription("D-" + id);
        item.setImagePath("/img/" + id + ".png");
        item.setPriceMinor(100L + id);
        return item;
    }

    private static CartItemRow cartItemRow(Long id, Integer count) {
        return new CartItemRow() {

            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public String getImagePath() {
                return null;
            }

            @Override
            public Long getPriceMinor() {
                return null;
            }

            @Override
            public Integer getCount() {
                return count;
            }

        };
    }

}
