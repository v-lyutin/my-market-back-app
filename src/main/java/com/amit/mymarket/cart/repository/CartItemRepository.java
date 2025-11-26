package com.amit.mymarket.cart.repository;

import com.amit.mymarket.cart.repository.projection.CartItemRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartItemRepository extends ReactiveCrudRepository<com.amit.mymarket.cart.domain.entity.CartItem, Long> {

    @Query(value = """
            select items.id as id,
                   items.title as title,
                   items.description as description,
                   items.img_path as imagePath,
                   items.price_minor as priceMinor,
                   carts_items.quantity as quantity
            from shop.carts
            join shop.carts_items on carts_items.cart_id = carts.id
            join shop.items on items.id = carts_items.item_id
            where carts.session_id = :sessionId and carts.status = 'ACTIVE'
            order by lower(items.title) asc, items.id asc
            """)
    Flux<CartItemRow> findCartItems(String sessionId);

    @Query(value = """
            select coalesce(sum(carts_items.quantity * items.price_minor), 0)
            from shop.carts
            join shop.carts_items on carts_items.cart_id = carts.id
            join shop.items on items.id = carts_items.item_id
            where carts.session_id = :sessionId and carts.status = 'ACTIVE'
            """)
    Mono<Long> calculateCartTotalPrice(String sessionId);

    @Query(value = """
            insert into shop.carts_items (cart_id, item_id, quantity)
            values (:cartId, :itemId, 1)
            on conflict (cart_id, item_id)
            do update set
                quantity = shop.carts_items.quantity + 1
            """)
    Mono<Integer> incrementItemQuantity(long cartId, long itemId);

    @Query(value = """
            delete from shop.carts_items
            where cart_id = :cartId
              and item_id = :itemId
              and quantity = 1
            """)
    Mono<Integer> deleteWhenItemQuantityIsOne(long cartId, long itemId);

    @Query(value = """
            update shop.carts_items
               set quantity = quantity - 1
             where cart_id = :cartId
               and item_id = :itemId
               and quantity > 1
            """)
    Mono<Integer> decrementWhenItemQuantityGreaterThanOne(long cartId, long itemId);

    @Query(value = """
            delete from shop.carts_items
            where cart_id = :cartId and item_id = :itemId
            """)
    Mono<Integer> deleteCartItem(long cartId, long itemId);

    @Query("""
        DELETE FROM shop.carts_items
        WHERE cart_id = :cartId
        """)
    Mono<Void> deleteByCartId(Long cartId);

}
