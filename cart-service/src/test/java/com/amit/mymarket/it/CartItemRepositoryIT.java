package com.amit.mymarket.it;

import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private CartItemRepository cartItemRepository;

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
                                (1, 'Apple',  'Fresh green apple', '/images/apple.png',  100),
                                (2, 'Banana', 'Yellow banana', '/images/banana.png', 50),
                                (3, 'Carrot', 'Orange carrot', '/images/carrot.png', 75)
                                """)
                        .fetch()
                        .rowsUpdated())
                // carts
                .then(this.databaseClient.sql("""
                                insert into shop.carts (id, session_id, status) values
                                (1, 'session-123', 'ACTIVE'),
                                (2, 'session-123', 'ABANDONED'),
                                (3, 'another-session', 'ACTIVE')
                                """)
                        .fetch()
                        .rowsUpdated())
                // carts_items
                .then(this.databaseClient.sql("""
                                insert into shop.carts_items (cart_id, item_id, quantity) values
                                (1, 1, 1),  -- Apple in active cart (session-123)
                                (1, 2, 2),  -- Banana
                                (1, 3, 3),  -- Carrot
                                (2, 1, 10), -- Apple in ABANDONED cart
                                (3, 1, 2)   -- Apple in another-session cart
                                """)
                        .fetch()
                        .rowsUpdated())
                .then();
        setupFlow.block();
    }

    @AfterEach
    void cleanUpTestData() {
        Mono<Void> cleanupFlow = this.databaseClient.sql("delete from shop.carts_items")
                .fetch()
                .rowsUpdated()
                .then(this.databaseClient.sql("delete from shop.carts")
                        .fetch()
                        .rowsUpdated())
                .then(this.databaseClient.sql("delete from shop.items")
                        .fetch()
                        .rowsUpdated())
                .then();
        cleanupFlow.block();
    }

    @Test
    @DisplayName(value = "Should return cart items for active cart with given session identifier sorted by title")
    void findCartItems_shouldReturnActiveCartItemsSortedByTitle() {
        String sessionId = "session-123";

        Flux<CartItemRow> cartItemRowFlux = this.cartItemRepository.findCartItems(sessionId);

        StepVerifier.create(cartItemRowFlux.collectList())
                .assertNext(cartItemRowList -> {
                    assertThat(cartItemRowList)
                            .hasSize(3)
                            .extracting(CartItemRow::title)
                            .containsExactly("Apple", "Banana", "Carrot");

                    CartItemRow firstCartItemRow = cartItemRowList.get(0);
                    CartItemRow secondCartItemRow = cartItemRowList.get(1);
                    CartItemRow thirdCartItemRow = cartItemRowList.get(2);

                    assertThat(firstCartItemRow.quantity()).isEqualTo(1);
                    assertThat(secondCartItemRow.quantity()).isEqualTo(2);
                    assertThat(thirdCartItemRow.quantity()).isEqualTo(3);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should calculate total cart price for active cart with given session identifier")
    void calculateCartTotalPrice_shouldReturnTotalPriceForActiveCart() {
        String sessionId = "session-123";

        Mono<Long> totalPriceMono = this.cartItemRepository.calculateCartTotalPrice(sessionId);

        // 1 * 100 (Apple) + 2 * 50 (Banana) + 3 * 75 (Carrot) = 425
        StepVerifier.create(totalPriceMono)
                .assertNext(totalPrice -> assertThat(totalPrice).isEqualTo(425L))
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should increase quantity when cart item already exists for given cart identifier and item identifier")
    void incrementItemQuantity_shouldIncreaseQuantityWhenCartItemAlreadyExists() {
        long cartId = 1L;   // session-123
        long itemId = 1L;   // Apple
        String sessionId = "session-123";

        Mono<List<CartItemRow>> cartItemRowsAfterIncrementMono = this.cartItemRepository.incrementItemQuantity(cartId, itemId)
                .thenMany(this.cartItemRepository.findCartItems(sessionId))
                .collectList();

        StepVerifier.create(cartItemRowsAfterIncrementMono)
                .assertNext(cartItemRowList -> {
                    assertThat(cartItemRowList).hasSize(3);

                    CartItemRow appleCartItemRow = cartItemRowList.stream()
                            .filter(cartItemRow -> "Apple".equals(cartItemRow.title()))
                            .findFirst()
                            .orElseThrow();

                    assertThat(appleCartItemRow.quantity()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should decrease quantity but keep cart item when quantity is greater than one")
    void decrementWhenItemQuantityGreaterThanOne_shouldDecreaseQuantityAndKeepCartItem() {
        long cartId = 1L;   // session-123
        long itemId = 3L;   // Carrot, quantity = 3
        String sessionId = "session-123";

        Mono<List<CartItemRow>> cartItemRowsAfterDecrementMono = this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(cartId, itemId)
                .thenMany(this.cartItemRepository.findCartItems(sessionId))
                .collectList();

        StepVerifier.create(cartItemRowsAfterDecrementMono)
                .assertNext(cartItemRowList -> {
                    assertThat(cartItemRowList).hasSize(3);

                    CartItemRow carrotCartItemRow = cartItemRowList.stream()
                            .filter(cartItemRow -> "Carrot".equals(cartItemRow.title()))
                            .findFirst()
                            .orElseThrow();

                    assertThat(carrotCartItemRow.quantity()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should delete cart item when deleteCartItem is called for given cart identifier and item identifier")
    void deleteCartItem_shouldDeleteCartItemRegardlessOfQuantity() {
        long cartId = 1L;   // session-123
        long itemId = 2L;   // Banana, quantity = 2
        String sessionId = "session-123";

        Mono<List<CartItemRow>> cartItemRowsAfterDeleteMono = this.cartItemRepository.deleteCartItem(cartId, itemId)
                .thenMany(cartItemRepository.findCartItems(sessionId))
                .collectList();

        StepVerifier.create(cartItemRowsAfterDeleteMono)
                .assertNext(cartItemRowList -> {
                    assertThat(cartItemRowList)
                            .extracting(CartItemRow::title)
                            .containsExactly("Apple", "Carrot");

                    boolean hasBananaCartItem = cartItemRowList.stream()
                            .anyMatch(cartItemRow -> "Banana".equals(cartItemRow.title()));

                    assertThat(hasBananaCartItem).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should delete cart item when quantity is one using deleteWhenItemQuantityIsOne")
    void deleteWhenItemQuantityIsOne_shouldDeleteCartItemWhenQuantityIsOne() {
        long cartId = 1L;   // session-123
        long itemId = 1L;   // Apple, quantity = 1
        String sessionId = "session-123";

        Mono<List<CartItemRow>> cartItemRowsAfterConditionalDeleteMono = this.cartItemRepository.deleteWhenItemQuantityIsOne(cartId, itemId)
                .thenMany(this.cartItemRepository.findCartItems(sessionId))
                .collectList();

        StepVerifier.create(cartItemRowsAfterConditionalDeleteMono)
                .assertNext(cartItemRowList -> {
                    assertThat(cartItemRowList)
                            .extracting(CartItemRow::title)
                            .containsExactly("Banana", "Carrot");

                    boolean hasAppleCartItem = cartItemRowList.stream()
                            .anyMatch(cartItemRow -> "Apple".equals(cartItemRow.title()));

                    assertThat(hasAppleCartItem).isFalse();
                })
                .verifyComplete();
    }

}
