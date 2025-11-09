package com.amit.mymarket.order.repository;

import com.amit.mymarket.order.entity.OrderItem;
import com.amit.mymarket.order.entity.OrderItemId;
import com.amit.mymarket.order.repository.projection.OrderItemSnapshotRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    @Query(value = """
            select orders_items.item_id as id,
                   orders_items.title_snapshot as title,
                   orders_items.price_minor_snapshot as price,
                   orders_items.quantity as count
            from shop.orders_items
            where orders_items.order_id = :orderId
            order by orders_items.title_snapshot asc, orders_items.item_id asc
            """,
            nativeQuery = true)
    List<OrderItemSnapshotRow> findOrderItems(@Param(value = "orderId") long orderId);

}
