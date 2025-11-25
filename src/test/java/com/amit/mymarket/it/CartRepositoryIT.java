package com.amit.mymarket.it;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.type.CartStatus;
import com.amit.mymarket.cart.repository.CartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName(value = "CartRepository integration tests")
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        statements = {
                "insert into shop.carts (id, session_id) values (1, 'session-123');",
                "insert into shop.carts (id, session_id, status) values (2, 'session-123', 'ORDERED');",
                "insert into shop.carts (id, session_id) values (3, 'other-session');"
        }
)
@Sql(
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
        statements = {
                "delete from shop.carts;"
        }
)
class CartRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private CartRepository cartRepository;

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
