package com.amit.mymarket.order.repository;

import com.amit.mymarket.order.entity.Order;
import com.amit.mymarket.order.repository.projection.OrderHeaderRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = """
            select orders.id as id,
                   orders.total_minor as totalMinor
            from shop.orders
            where orders.session_id = :sessionId
            order by orders.created_at desc
            """,
            nativeQuery = true)
    List<OrderHeaderRow> findOrdersBySession(@Param(value = "sessionId") String sessionId);

    @Query(value = """
            select orders.id as id,
                   orders.total_minor as totalMinor
            from shop.orders
            where orders.id = :orderId and orders.session_id = :sessionId
            """,
            nativeQuery = true)
    Optional<OrderHeaderRow> findOrderHeader(@Param(value = "orderId") long orderId, @Param(value = "sessionId") String sessionId);

}
