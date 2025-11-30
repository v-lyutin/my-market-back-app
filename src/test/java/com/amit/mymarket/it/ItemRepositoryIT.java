package com.amit.mymarket.it;

import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.repository.projection.ItemWithQuantity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUpTestData() {
        Mono<Void> setupFlow = this.databaseClient.sql("delete from shop.carts_items")
                .fetch()
                .rowsUpdated()
                .then(this.databaseClient.sql("delete from shop.carts")
                        .fetch()
                        .rowsUpdated())
                .then(this.databaseClient.sql("delete from shop.items")
                        .fetch()
                        .rowsUpdated())
                // items
                .then(this.databaseClient.sql("""
                                insert into shop.items (id, title, description, img_path, price_minor) values
                                (1, 'Apple', 'Fresh green apple', '/images/apple.png',  100),
                                (2, 'Banana', 'Yellow banana', '/images/banana.png', 50),
                                (3, 'Carrot', 'Orange carrot', '/images/carrot.png', 75)
                                """)
                        .fetch()
                        .rowsUpdated())
                // carts
                .then(this.databaseClient.sql("""
                                insert into shop.carts (id, session_id) values
                                (1, 'session-123'),
                                (2, 'other-session')
                                """)
                        .fetch()
                        .rowsUpdated())
                // carts_items
                .then(this.databaseClient.sql("""
                                insert into shop.carts_items (cart_id, item_id, quantity) values
                                (1, 1, 2),
                                (1, 3, 5),
                                (2, 2, 7)
                                """)
                        .fetch()
                        .rowsUpdated())
                .then();
        setupFlow.block();
    }

    @AfterEach
    void cleanUpTestData() {
        Mono<Void> cleanFlow =
                this.databaseClient.sql("delete from shop.carts_items")
                        .fetch()
                        .rowsUpdated()
                        .then(this.databaseClient.sql("delete from shop.carts")
                                .fetch()
                                .rowsUpdated())
                        .then(this.databaseClient.sql("delete from shop.items")
                                .fetch()
                                .rowsUpdated())
                        .then();
        cleanFlow.block();
    }

    @Test
    @DisplayName(value = "Should return all items with correct quantities for active session without searchQuery filter")
    void searchItemsWithQuantity_shouldReturnAllItemsForSessionWithoutSearchFilter() {
        String sessionId = "session-123";
        String searchQuery = null;
        String sortType = "ALPHA";
        long limit = 10L;
        long offset = 0L;

        Flux<ItemWithQuantity> itemWithQuantityFlux = this.itemRepository.searchItemsWithQuantity(
                sessionId,
                searchQuery,
                sortType,
                limit,
                offset
        );

        StepVerifier.create(itemWithQuantityFlux)
                .assertNext(firstItem -> {
                    assertThat(firstItem.title()).isEqualTo("Apple");
                    assertThat(firstItem.quantity()).isEqualTo(2);
                })
                .assertNext(secondItem -> {
                    assertThat(secondItem.title()).isEqualTo("Banana");
                    assertThat(secondItem.quantity()).isEqualTo(0);
                })
                .assertNext(thirdItem -> {
                    assertThat(thirdItem.title()).isEqualTo("Carrot");
                    assertThat(thirdItem.quantity()).isEqualTo(5);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should filter items by searchQuery term in title or description")
    void searchItemsWithQuantity_shouldFilterBySearchTermInTitleOrDescription() {
        String sessionId = "session-123";
        String searchQuery = "apple";
        String sortType = "ALPHA";
        long limit = 10L;
        long offset = 0L;

        Flux<ItemWithQuantity> itemWithQuantityFlux = this.itemRepository.searchItemsWithQuantity(
                sessionId,
                searchQuery,
                sortType,
                limit,
                offset
        );

        StepVerifier.create(itemWithQuantityFlux)
                .expectNextMatches(itemWithQuantity -> "Apple".equals(itemWithQuantity.title()) && itemWithQuantity.quantity() == 2)
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should sort items alphabetically when sort type is ALPHA")
    void searchItemsWithQuantity_shouldSortAlphabeticallyWhenSortTypeIsAlpha() {
        String sessionId = "session-123";
        String searchQuery = null;
        String sortType = "ALPHA";
        long limit = 10L;
        long offset = 0L;

        Flux<ItemWithQuantity> itemWithQuantityFlux = this.itemRepository.searchItemsWithQuantity(
                sessionId,
                searchQuery,
                sortType,
                limit,
                offset
        );

        StepVerifier.create(itemWithQuantityFlux.collectList())
                .assertNext(itemWithQuantityList -> {
                    assertThat(itemWithQuantityList)
                            .extracting(ItemWithQuantity::title)
                            .containsExactly("Apple", "Banana", "Carrot");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should sort items by price when sort type is PRICE")
    void searchItemsWithQuantity_shouldSortByPriceWhenSortTypeIsPrice() {
        String sessionId = "session-123";
        String searchQuery = null;
        String sortType = "PRICE";
        long limit = 10L;
        long offset = 0L;

        Flux<ItemWithQuantity> itemWithQuantity = this.itemRepository.searchItemsWithQuantity(
                sessionId,
                searchQuery,
                sortType,
                limit,
                offset
        );

        StepVerifier.create(itemWithQuantity.collectList())
                .assertNext(itemWithQuantityList -> {
                    assertThat(itemWithQuantityList)
                            .extracting(ItemWithQuantity::priceMinor)
                            .containsExactly(50L, 75L, 100L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should apply limit and offset correctly")
    void searchItemsWithQuantity_shouldApplyLimitAndOffsetCorrectly() {
        String sessionId = "session-123";
        String searchQuery = null;
        String sortType = "ALPHA";
        long limit = 1L;
        long offset = 1L;

        Flux<ItemWithQuantity> itemWithQuantityFlux = this.itemRepository.searchItemsWithQuantity(
                sessionId,
                searchQuery,
                sortType,
                limit,
                offset
        );

        StepVerifier.create(itemWithQuantityFlux)
                .assertNext(itemWithQuantity -> assertThat(itemWithQuantity.title()).isEqualTo("Banana"))
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return item with zero quantity when item is not in session cart")
    void findItemWithQuantity_shouldReturnItemWithZeroQuantityWhenItemIsNotInSessionCart() {
        long itemId = 2L;
        String sessionId = "session-123";

        Mono<ItemWithQuantity> itemWithQuantityMono = this.itemRepository.findItemWithQuantity(
                itemId,
                sessionId
        );

        StepVerifier.create(itemWithQuantityMono)
                .assertNext(itemWithQuantity -> {
                    assertThat(itemWithQuantity.title()).isEqualTo("Banana");
                    assertThat(itemWithQuantity.quantity()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return item with correct quantity when item is in session cart")
    void findItemWithQuantity_shouldReturnItemWithCorrectQuantityWhenItemIsInSessionCart() {
        long itemId = 1L; // Apple with quantity (in cart) = 2 for session-123
        String sessionId = "session-123";

        Mono<ItemWithQuantity> itemWithQuantityMono = this.itemRepository.findItemWithQuantity(
                itemId,
                sessionId
        );

        StepVerifier.create(itemWithQuantityMono)
                .assertNext(itemWithQuantity -> {
                    assertThat(itemWithQuantity.title()).isEqualTo("Apple");
                    assertThat(itemWithQuantity.quantity()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should quantity items matching searchQuery query")
    void countItemsBySearchQuery_shouldReturnNumberOfMatchingItems() {
        String searchQuery = "a"; // Apple, Banana, Carrot

        Mono<Long> itemCountMono = this.itemRepository.countItemsBySearchQuery(searchQuery);

        StepVerifier.create(itemCountMono)
                .assertNext(itemCount -> assertThat(itemCount).isEqualTo(3L))
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should quantity all items when searchQuery query is null")
    void countItemsBySearchQuery_shouldReturnAllItemsWhenSearchQueryIsNull() {
        String searchQuery = null;

        Mono<Long> itemCountMono = this.itemRepository.countItemsBySearchQuery(searchQuery);

        StepVerifier.create(itemCountMono)
                .assertNext(itemCount -> assertThat(itemCount).isEqualTo(3L))
                .verifyComplete();
    }

}
