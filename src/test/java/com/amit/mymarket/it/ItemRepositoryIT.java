package com.amit.mymarket.it;

import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.repository.projection.ItemWithCountRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName(value = "Should return all items with correct counts and default ordering by id when search and sort are not specified")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (2,'banana','Yellow','/img/banana.png',50)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (3,'Apricot','Orange','/img/orange.png',150)",
            "insert into shop.carts (id, session_id) values (10,'session-1')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (10,1,2)",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (10,3,1)"
    })
    void findCatalogWithCounts_shouldReturnAllItemsWithCorrectCountsAndDefaultOrderingByIdWhenSearchAndSortAreNotSpecified() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ItemWithCountRow> itemPage = this.itemRepository.findCatalogWithCounts("session-1", null, null, pageable);
        assertThat(itemPage.getTotalElements()).isEqualTo(3);

        List<ItemWithCountRow> itemRows = itemPage.getContent();
        assertThat(itemRows).extracting(ItemWithCountRow::getId).containsExactly(1L, 2L, 3L);

        ItemWithCountRow appleRow = itemRows.stream().filter(row -> row.getId() == 1L).findFirst().orElseThrow();
        ItemWithCountRow bananaRow = itemRows.stream().filter(row -> row.getId() == 2L).findFirst().orElseThrow();
        ItemWithCountRow apricotRow = itemRows.stream().filter(row -> row.getId() == 3L).findFirst().orElseThrow();

        assertThat(appleRow.getCount()).isEqualTo(2);
        assertThat(bananaRow.getCount()).isEqualTo(0);
        assertThat(apricotRow.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "Should find items by title and description ignoring case when search parameter is provided")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (2,'banana','Yellow','/img/banana.png',50)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (3,'Apricot','Orange','/img/apricot.png',150)"
    })
    void findCatalogWithCounts_shouldFindItemsByTitleAndDescriptionIgnoringCaseWhenSearchParameterIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ItemWithCountRow> searchByTitlePage = this.itemRepository.findCatalogWithCounts("sess-1", "apple", null, pageable);
        assertThat(searchByTitlePage.getTotalElements()).isEqualTo(1);
        assertThat(searchByTitlePage.getContent().getFirst().getTitle()).isEqualTo("Apple");

        Page<ItemWithCountRow> searchByDescriptionPage = this.itemRepository.findCatalogWithCounts("sess-1", "yellow", null, pageable);
        assertThat(searchByDescriptionPage.getTotalElements()).isEqualTo(1);
        assertThat(searchByDescriptionPage.getContent().getFirst().getTitle()).isEqualTo("banana");
    }

    @Test
    @DisplayName(value = "Should return items sorted alphabetically by title when sort parameter is ALPHA")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (2,'banana','Yellow','/img/banana.png',50)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (3,'Apricot','Orange','/img/apricot.png',150)"
    })
    void findCatalogWithCounts_shouldReturnItemsSortedAlphabeticallyByTitleWhenSortParameterIsAlpha() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ItemWithCountRow> itemPage = this.itemRepository.findCatalogWithCounts("session-1", null, "ALPHA", pageable);
        List<ItemWithCountRow> itemRows = itemPage.getContent();

        assertThat(itemRows)
                .extracting(ItemWithCountRow::getTitle)
                .containsExactly("Apple", "Apricot", "banana");
    }

    @Test
    @DisplayName(value = "Should return items sorted by priceMinor when sort parameter is PRICE")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (2,'banana','Yellow','/img/banana.png',50)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (3,'Apricot','Orange','/img/apricot.png',150)"
    })
    void findCatalogWithCounts_shouldReturnItemsSortedByPriceWhenSortParameterIsPrice() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ItemWithCountRow> itemPage = this.itemRepository.findCatalogWithCounts("session-1", null, "PRICE", pageable);
        List<ItemWithCountRow> itemRows = itemPage.getContent();

        assertThat(itemRows).extracting(ItemWithCountRow::getPriceMinor)
                .containsExactly(50L, 100L, 150L);
    }

    @Test
    @DisplayName(value = "Should return item with correct count when the item exists in the active cart for the given session")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1,'Apple','Red apple','/img/apple.png',100)",
            "insert into shop.carts (id, session_id) values (10,'session-1')",
            "insert into shop.carts_items (cart_id, item_id, quantity) values (10,1,2)"
    })
    void findItemWithCount_shouldReturnItemWithCorrectCountWhenTheItemExistsInTheActiveCartForTheGivenSession() {
        ItemWithCountRow itemRow = this.itemRepository.findItemWithCount(1L, "session-1");

        assertThat(itemRow.getId()).isEqualTo(1L);
        assertThat(itemRow.getCount()).isEqualTo(2);
        assertThat(itemRow.getImagePath()).isEqualTo("/img/apple.png");
    }

    @Test
    @DisplayName(value = "Should return item with zero count when the item does not exist in the active cart for the given session")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (2,'banana','Yellow','/img/banana.png',50)",
            "insert into shop.carts (id, session_id) values (10,'session-1')"
    })
    void shouldReturnItemWithZeroCountWhenTheItemDoesNotExistInTheActiveCartForTheGivenSession() {
        ItemWithCountRow itemRow = this.itemRepository.findItemWithCount(2L, "session-1");

        assertThat(itemRow.getId()).isEqualTo(2L);
        assertThat(itemRow.getCount()).isEqualTo(0);
        assertThat(itemRow.getImagePath()).isEqualTo("/img/banana.png");
    }

}
