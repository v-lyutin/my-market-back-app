package com.amit.mymarket.it;

import com.amit.mymarket.cart.domain.entity.Cart;
import com.amit.mymarket.cart.domain.entity.enums.CartStatus;
import com.amit.mymarket.cart.repository.CartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CartRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private CartRepository cartRepository;

    @Test
    @DisplayName(value = "Should find cart by session id and status when the cart exists")
    @Sql(statements = {
            "insert into shop.carts (id, session_id) values (100, 'session-1')",
            "insert into shop.carts (id, session_id, status) values (101, 'session-1', 'ABANDONED')",
            "insert into shop.carts (id, session_id) values (102, 'session-2')"
    })
    void findBySessionIdAndStatus_shouldFindCartBySessionIdAndStatusWhenTheCartExists() {
        Optional<Cart> optionalCart = this.cartRepository.findBySessionIdAndStatus("session-1", CartStatus.ACTIVE);

        assertThat(optionalCart).isPresent();
        Cart cart = optionalCart.orElseThrow();

        assertThat(cart.getSessionId()).isEqualTo("session-1");
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
    }

    @Test
    @DisplayName(value = "Should return empty when cart with given session id does not have the requested status")
    @Sql(statements = {
            "insert into shop.carts (id, session_id, status) values (200, 'session-3', 'ABANDONED')"
    })
    void findBySessionIdAndStatus_shouldReturnEmptyWhenCartWithGivenSessionIdDoesNotHaveTheRequestedStatus() {
        Optional<Cart> cart = this.cartRepository.findBySessionIdAndStatus("session-3", CartStatus.ACTIVE);

        assertThat(cart).isNotPresent();
    }

    @Test
    @DisplayName(value = "Should return empty when no cart exists for the given session id")
    void findBySessionIdAndStatus_shouldReturnEmptyWhenNoCartExistsForTheGivenSessionId() {
        Optional<Cart> cart = this.cartRepository.findBySessionIdAndStatus("missing-session", CartStatus.ACTIVE);

        assertThat(cart).isNotPresent();
    }

}
