package com.amit.mymarket.it;

import com.amit.mymarket.order.repository.OrderItemRepository;
import com.amit.mymarket.order.repository.projection.OrderItemSnapshotRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName(value = "Should return order items for given order id ordered by title ascending and then by item id ascending")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1001,'Apple','Apple','/img/apple.png',100)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (1002,'Apricot','Apricot','/img/apricot.png',150)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (1003,'Banana','Banana','/img/banana.png',50)",
            "insert into shop.orders (id, session_id, total_minor) values (5001,'session-x',0)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5001,1003,'Banana',50,2)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5001,1001,'Apple',100,1)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5001,1002,'Apricot',150,3)"
    })
    void findOrderItems_shouldReturnOrderItemsForGivenOrderIdOrderedByTitleAscendingAndThenByItemIdAscending() {
        List<OrderItemSnapshotRow> orderItems = this.orderItemRepository.findOrderItems(5001L);

        assertThat(orderItems).hasSize(3);
        assertThat(orderItems).extracting(OrderItemSnapshotRow::getTitle)
                .containsExactly("Apple", "Apricot", "Banana");

        OrderItemSnapshotRow first = orderItems.getFirst();
        assertThat(first.getId()).isEqualTo(1001L);
        assertThat(first.getPrice()).isEqualTo(100L);
        assertThat(first.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "Should break ties by item id when titles are equal")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1101,'Milk','Milk','/img/milk.png',120)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (1102,'Milk','Milk','/img/milk2.png',130)",
            "insert into shop.orders (id, session_id, total_minor) values (5101,'session-y',0)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5101,1102,'Milk',130,1)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5101,1101,'Milk',120,2)"
    })
    void findOrderItems_shouldBreakTiesByItemIdWhenTitlesAreEqual() {
        List<OrderItemSnapshotRow> orderItems = this.orderItemRepository.findOrderItems(5101L);

        assertThat(orderItems).hasSize(2);
        assertThat(orderItems)
                .extracting(OrderItemSnapshotRow::getId)
                .containsExactly(1101L, 1102L);
        assertThat(orderItems)
                .extracting(OrderItemSnapshotRow::getTitle)
                .containsExactly("Milk", "Milk");
    }

    @Test
    @DisplayName(value = "Should return empty list when order has no items")
    @Sql(statements = {
            "insert into shop.orders (id, session_id, total_minor) values (5201,'session-empty',0)"
    })
    void findOrderItems_shouldReturnEmptyListWhenOrderHasNoItems() {
        List<OrderItemSnapshotRow> orderItems = this.orderItemRepository.findOrderItems(5201L);
        assertThat(orderItems).isEmpty();
    }

    @Test
    @DisplayName(value = "Should not mix items of different orders")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1201,'Apple','Red apple','/img/apple.png',10)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (1202,'Banana','Banana','/img/banana.png',20)",
            "insert into shop.orders (id, session_id, total_minor) values (5301,'session-a',0)",
            "insert into shop.orders (id, session_id, total_minor) values (5302,'session-b',0)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5301,1201,'Apple',10,1)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5302,1202,'Banana',20,2)"
    })
    void findOrderItems_shouldNotMixItemsOfDifferentOrders() {
        List<OrderItemSnapshotRow> orderItems = this.orderItemRepository.findOrderItems(5301L);

        assertThat(orderItems).hasSize(1);
        OrderItemSnapshotRow order = orderItems.getFirst();
        assertThat(order.getId()).isEqualTo(1201L);
        assertThat(order.getTitle()).isEqualTo("Apple");
        assertThat(order.getPrice()).isEqualTo(10L);
        assertThat(order.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "Should cascade delete order items when the order is deleted (ON DELETE CASCADE)")
    @Sql(statements = {
            "insert into shop.items (id, title, description, img_path, price_minor) values (1301,'X','X','/img/x.png',10)",
            "insert into shop.items (id, title, description, img_path, price_minor) values (1302,'Y','Y','/img/y.png',20)",
            "insert into shop.orders (id, session_id, total_minor) values (5401,'session-c',0)",
            "insert into shop.orders (id, session_id, total_minor) values (5402,'session-d',0)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5401,1301,'X',10,1)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5401,1302,'Y',20,2)",
            "insert into shop.orders_items (order_id, item_id, title_snapshot, price_minor_snapshot, quantity) values (5402,1301,'X',10,3)"
    })
    void shouldCascadeDeleteOrderItemsWhenTheOrderIsDeletedOnDeleteCascade() {
        Integer rowsBeforeDeleteFirstOrder = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.orders_items where order_id = 5401",
                Integer.class
        );
        Integer rowsBeforeDeleteSecondOrder = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.orders_items where order_id = 5402",
                Integer.class
        );

        assertThat(rowsBeforeDeleteFirstOrder).isEqualTo(2);
        assertThat(rowsBeforeDeleteSecondOrder).isEqualTo(1);

        this.jdbcTemplate.update("delete from shop.orders where id = 5401");

        Integer rowsAfterDeleteFirstOrder = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.orders_items where order_id = 5401",
                Integer.class
        );
        Integer rowsAfterDeleteSecondOrder = this.jdbcTemplate.queryForObject(
                "select count(*) from shop.orders_items where order_id = 5402",
                Integer.class
        );

        assertThat(rowsAfterDeleteFirstOrder).isEqualTo(0);
        assertThat(rowsAfterDeleteSecondOrder).isEqualTo(1);
    }

}
