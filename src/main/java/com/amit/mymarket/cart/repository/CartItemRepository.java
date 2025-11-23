package com.amit.mymarket.cart.repository;

import com.amit.mymarket.cart.domain.entity.CartItem;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    @Query(value = """
            select items.id as id,
                   items.title as title,
                   items.description as description,
                   items.img_path as imagePath,
                   items.price_minor as priceMinor,
                   carts_items.quantity as count
            from shop.carts
            join shop.carts_items on carts_items.cart_id = carts.id
            join shop.items on items.id = carts_items.item_id
            where carts.session_id = :sessionId and carts.status = 'ACTIVE'
            order by lower(items.title) asc, items.id asc
            """,
            nativeQuery = true)
    List<CartItemRow> findCartItems(@Param(value = "sessionId") String sessionId);

    @Query(value = """
            select coalesce(sum(carts_items.quantity * items.price_minor), 0)
            from shop.carts
            join shop.carts_items on carts_items.cart_id = carts.id
            join shop.items on items.id = carts_items.item_id
            where carts.session_id = :sessionId and carts.status = 'ACTIVE'
            """,
            nativeQuery = true)
    Long calculateCartTotal(@Param(value = "sessionId") String sessionId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            insert into shop.carts_items (cart_id, item_id, quantity, updated_at)
            values (:cartId, :itemId, 1, now())
            on conflict (cart_id, item_id)
            do update set
                quantity = shop.carts_items.quantity + 1,
                updated_at = now()
            """,
            nativeQuery = true)
    int incrementItemQuantity(@Param("cartId") long cartId, @Param("itemId") long itemId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            delete from shop.carts_items
            where cart_id = :cartId
              and item_id = :itemId
              and quantity = 1
            """,
            nativeQuery = true)
    int deleteWhenItemQuantityIsOne(@Param("cartId") long cartId, @Param("itemId") long itemId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update shop.carts_items
               set quantity = quantity - 1,
                   updated_at = now()
             where cart_id = :cartId
               and item_id = :itemId
               and quantity > 1
            """,
            nativeQuery = true)
    int decrementWhenItemQuantityGreaterThanOne(@Param("cartId") long cartId, @Param("itemId") long itemId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            delete from shop.carts_items
            where cart_id = :cartId and item_id = :itemId
            """,
            nativeQuery = true)
    int deleteCartItem(@Param(value = "cartId") long cartId, @Param(value = "itemId") long itemId);

}
