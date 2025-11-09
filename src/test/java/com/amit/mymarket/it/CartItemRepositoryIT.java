package com.amit.mymarket.it;

import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName(value = "Should return cart items ordered case-insensitively by title and then by id when active cart exists")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (2,'banana','Yellow banana','/img/banana.png',50)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (3,'Apricot','Orange apricot','/img/apricot.png',150)",
            "insert into shop.carts (id, session_id) values (10,'session-1')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (10,1,2)",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (10,3,1)"
    })
    void findCartItems_shouldReturnCartItemsOrderedCaseInsensitivelyByTitleAndThenByIdWhenActiveCartExists() {
        List<CartItemRow> cartItemRows = this.cartItemRepository.findCartItems("session-1");

        assertThat(cartItemRows)
                .extracting(CartItemRow::getTitle)
                .containsExactly("Apple", "Apricot");

        CartItemRow cartItemRowWithApple = cartItemRows.get(0);
        CartItemRow cartItemRowWithApricot = cartItemRows.get(1);

        assertThat(cartItemRowWithApple.getId()).isEqualTo(1L);
        assertThat(cartItemRowWithApple.getCount()).isEqualTo(2);
        assertThat(cartItemRowWithApple.getImagePath()).isEqualTo("/img/apple.png");
        assertThat(cartItemRowWithApple.getPriceMinor()).isEqualTo(100L);

        assertThat(cartItemRowWithApricot.getId()).isEqualTo(3L);
        assertThat(cartItemRowWithApricot.getCount()).isEqualTo(1);
        assertThat(cartItemRowWithApricot.getImagePath()).isEqualTo("/img/apricot.png");
        assertThat(cartItemRowWithApricot.getPriceMinor()).isEqualTo(150L);
    }

    @Test
    @DisplayName(value = "Should return empty list when active cart does not exist for the given session")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.carts (id, session_id, status) values (11,'session-2','ABANDONED')"
    })
    void findCartItems_shouldReturnEmptyListWhenActiveCartDoesNotExistForTheGivenSession() {
        List<CartItemRow> cartItemRows = this.cartItemRepository.findCartItems("session-1");
        assertThat(cartItemRows).isEmpty();
    }

    @Test
    @DisplayName(value = "Should return empty list of cart items for a valid active cart that has no items")
    @Sql(statements = {
            "insert into shop.carts (id, session_id) values (70,'session-9')"
    })
    void findCartItems_shouldReturnEmptyListOfCartItemsForAValidActiveCartThatHasNoItems() {
        List<CartItemRow> cartItemRows = this.cartItemRepository.findCartItems("session-9");
        assertThat(cartItemRows).isEmpty();
    }

    @Test
    @DisplayName(value = "Should calculate total price in minor units for the active cart of the given session")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (3,'Apricot','Orange apricot','/img/apricot.png',150)",
            "insert into shop.carts (id, session_id) values (20,'session-3')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (20,1,2)",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (20,3,1)"
    })
    void calculateCartTotal_shouldCalculateTotalPriceInMinorUnitsForTheActiveCartOfTheGivenSession() {
        Long totalMinor = this.cartItemRepository.calculateCartTotal("session-3");
        assertThat(totalMinor).isEqualTo(350L);
    }

    @Test
    @DisplayName(value = "Should return zero total when active cart is absent or has no items")
    @Sql(statements = {
            "insert into shop.carts (id, session_id) values (21,'session-4')"
    })
    void calculateCartTotal_shouldReturnZeroTotalWhenActiveCartIsAbsentOrHasNoItems() {
        Long totalMinorForMissingCart = this.cartItemRepository.calculateCartTotal("missing-session");
        Long totalMinorForEmptyCart = this.cartItemRepository.calculateCartTotal("session-4");

        assertThat(totalMinorForMissingCart).isEqualTo(0L);
        assertThat(totalMinorForEmptyCart).isEqualTo(0L);
    }

    @Test
    @DisplayName(value = "Should return zero total when there are items in a different session but not in the requested one")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (9,'Plum','Plum','/img/plum.png',70)",
            "insert into shop.carts (id, session_id) values (80,'session-x')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (80,9,5)"
    })
    void calculateCartTotal_shouldReturnZeroTotalWhenThereAreItemsInADifferentSessionButNotInTheRequestedOne() {
        Long totalMinor = this.cartItemRepository.calculateCartTotal("session-y");
        assertThat(totalMinor).isEqualTo(0L);
    }

    @Test
    @DisplayName(value = "Should insert a new cart item with quantity one when the pair does not exist and then increment existing item on subsequent call")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (5,'Mango','Sweet mango','/img/mango.png',300)",
            "insert into shop.carts (id, session_id) values (30,'session-5')"
    })
    void incrementItemQuantity_shouldInsertNewCartItemWithQuantityOneThenIncrementOnSubsequentCall() {
        int affectedRowsAfterInsert = this.cartItemRepository.incrementItemQuantity(30L, 5L);
        assertThat(affectedRowsAfterInsert).isEqualTo(1);

        int affectedRowsAfterUpdate = this.cartItemRepository.incrementItemQuantity(30L, 5L);
        assertThat(affectedRowsAfterUpdate).isEqualTo(1);

        Integer currentItemQuantity = this.jdbcTemplate.queryForObject(
                "select quantity from shop.carts_items where cart_id = 30 and item_id = 5",
                Integer.class
        );
        assertThat(currentItemQuantity).isEqualTo(2);
    }

    @Test
    @DisplayName(value = "Should delete row when quantity equals one and return one as affected rows count")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (701,'Pear','Green pear','/img/pear.png',130)",
            "insert into shop.carts (id, session_id) values (501,'session-del-1')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (501,701,1)"
    })
    void deleteWhenItemQuantityIsOne_shouldDeleteRowWhenQuantityEqualsOneAndReturnOneAsAffectedRowsCount() {
        int affectedRowsCount = this.cartItemRepository.deleteWhenItemQuantityIsOne(501L, 701L);
        assertThat(affectedRowsCount).isEqualTo(1);

        Integer rowsLeft = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.carts_items where cart_id = 501 and item_id = 701",
                Integer.class
        );
        assertThat(rowsLeft).isEqualTo(0);
    }

    @Test
    @DisplayName(value = "Should return zero and keep row when quantity does not equal one")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (702,'Apple','Red apple','/img/a.png',100)",
            "insert into shop.carts (id, session_id) values (502,'session-del-2')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (502,702,2)"
    })
    void deleteWhenItemQuantityIsOne_shouldReturnZeroAndKeepRowWhenQuantityDoesNotEqualOne() {
        int affectedRowsCount = this.cartItemRepository.deleteWhenItemQuantityIsOne(502L, 702L);
        assertThat(affectedRowsCount).isEqualTo(0);

        Integer currentQuantity = this.jdbcTemplate.queryForObject(
                "select quantity from shop.carts_items where cart_id = 502 and item_id = 702",
                Integer.class
        );
        assertThat(currentQuantity).isEqualTo(2);
    }

    @Test
    @DisplayName(value = "Should return zero for both operations when the cart item row does not exist")
    void deleteWhenItemQuantityIsOne_shouldReturnZeroForBothOperationsWhenTheCartItemRowDoesNotExist() {
        int deletedRowsCount = this.cartItemRepository.deleteWhenItemQuantityIsOne(9999L, 9999L);
        int decrementedRowsCount = this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(9999L, 9999L);

        assertThat(deletedRowsCount).isEqualTo(0);
        assertThat(decrementedRowsCount).isEqualTo(0);
    }

    @Test
    @DisplayName(value = "Should decrement quantity by one when quantity is greater than one and return one as affected rows count")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (703,'Mango','Sweet mango','/img/m.png',300)",
            "insert into shop.carts (id, session_id) values (503,'session-dec-1')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (503,703,3)"
    })
    void decrementWhenItemQuantityGreaterThanOne_shouldDecrementQuantityByOneWhenQuantityIsGreaterThanOneAndReturnOneAsAffectedRowsCount() {
        int affectedRowsCount = this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(503L, 703L);
        assertThat(affectedRowsCount).isEqualTo(1);

        Integer currentQuantity = this.jdbcTemplate.queryForObject(
                "select quantity from shop.carts_items where cart_id = 503 and item_id = 703",
                Integer.class
        );
        assertThat(currentQuantity).isEqualTo(2);
    }

    @Test
    @DisplayName(value = "Should return zero and keep quantity when quantity is not greater than one")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (704,'Orange','Orange','/img/o.png',120)",
            "insert into shop.carts (id, session_id) values (504,'session-dec-2')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (504,704,1)"
    })
    void decrementWhenItemQuantityGreaterThanOne_shouldReturnZeroAndKeepQuantityWhenQuantityIsNotGreaterThanOne() {
        int affectedRowsCount = this.cartItemRepository.decrementWhenItemQuantityGreaterThanOne(504L, 704L);
        assertThat(affectedRowsCount).isEqualTo(0);

        Integer currentQuantity = this.jdbcTemplate.queryForObject(
                "select quantity from shop.carts_items where cart_id = 504 and item_id = 704",
                Integer.class
        );
        assertThat(currentQuantity).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "Should delete the cart item row by cart id and item id and return one as affected rows count")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (801,'Tomato','Tomato','/img/tomato.png',40)",
            "insert into shop.carts (id, session_id) values (601,'session-del-ci-1')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (601,801,2)"
    })
    void deleteCartItem_shouldDeleteTheCartItemRowByCartIdAndItemIdAndReturnOneAsAffectedRowsCount() {
        int affectedRowsCount = this.cartItemRepository.deleteCartItem(601L, 801L);
        assertThat(affectedRowsCount).isEqualTo(1);

        Integer rowsLeft = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.carts_items where cart_id = 601 and item_id = 801",
                Integer.class
        );
        assertThat(rowsLeft).isEqualTo(0);
    }

    @Test
    @DisplayName(value = "Should return zero when the cart item row does not exist")
    void deleteCartItem_shouldReturnZeroWhenTheCartItemRowDoesNotExist() {
        int affectedRowsCount = this.cartItemRepository.deleteCartItem(9999L, 9999L);
        assertThat(affectedRowsCount).isEqualTo(0);
    }

    @Test
    @DisplayName(value = "Should delete only the targeted item and keep other items and carts intact")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (811,'Milk','Milk','/img/milk.png',120)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (812,'Bread','Bread','/img/bread.png',80)",
            "insert into shop.carts (id, session_id) values (611,'session-del-ci-2')",
            "insert into shop.carts (id, session_id) values (612,'session-del-ci-3')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (611,811,3)",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (611,812,1)",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (612,811,5)"
    })
    void deleteCartItem_shouldDeleteOnlyTheTargetedItemAndKeepOtherItemsAndCartsIntact() {
        int affectedRowsCount = this.cartItemRepository.deleteCartItem(611L, 811L);
        assertThat(affectedRowsCount).isEqualTo(1);

        Integer targetRowsLeft = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.carts_items where cart_id = 611 and item_id = 811",
                Integer.class
        );
        Integer siblingItemRowsLeft = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.carts_items where cart_id = 611 and item_id = 812",
                Integer.class
        );
        Integer otherCartRowsLeft = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.carts_items where cart_id = 612 and item_id = 811",
                Integer.class
        );

        assertThat(targetRowsLeft).isEqualTo(0);
        assertThat(siblingItemRowsLeft).isEqualTo(1);
        assertThat(otherCartRowsLeft).isEqualTo(1);
    }

}
