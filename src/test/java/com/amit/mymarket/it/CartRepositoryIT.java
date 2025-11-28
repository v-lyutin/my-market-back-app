package com.amit.mymarket.it;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CartRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private CartRepository cartRepository;

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
                .then(this.databaseClient.sql("""
                                insert into shop.carts (id, session_id, status) values
                                (1, 'session-123',  'ACTIVE'),
                                (2, 'session-123',  'ORDERED'),
                                (3, 'other-session', 'ACTIVE')
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
                .then();
        cleanupFlow.block();
    }

    @Test
    @DisplayName(value = "Should return cart when cart with given session identifier and status exists")
    void findBySessionIdAndStatus_shouldReturnCartWhenCartWithGivenSessionIdentifierAndStatusExists() {
        String sessionId = "session-123";
        CartStatus cartStatus = CartStatus.ACTIVE;

        Mono<Cart> cartMono = this.cartRepository.findBySessionIdAndStatus(sessionId, cartStatus);

        StepVerifier.create(cartMono)
                .assertNext(cart -> {
                    assertThat(cart.getId()).isEqualTo(1L);
                    assertThat(cart.getSessionId()).isEqualTo("session-123");
                    assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when cart with given session identifier exists but status does not match")
    void findBySessionIdAndStatus_shouldReturnEmptyResultWhenStatusDoesNotMatchForSessionIdentifier() {
        String sessionId = "session-123";
        CartStatus cartStatus = CartStatus.ABANDONED;

        Mono<Cart> cart = this.cartRepository.findBySessionIdAndStatus(sessionId, cartStatus);

        StepVerifier.create(cart).verifyComplete();
    }

    @Test
    @DisplayName(value = "Should return empty result when cart with given session identifier does not exist")
    void findBySessionIdAndStatus_shouldReturnEmptyResultWhenSessionIdentifierDoesNotExist() {
        String sessionId = "unknown-session";
        CartStatus cartStatus = CartStatus.ACTIVE;

        Mono<Cart> cart = this.cartRepository.findBySessionIdAndStatus(sessionId, cartStatus);

        StepVerifier.create(cart).verifyComplete();
    }

}
