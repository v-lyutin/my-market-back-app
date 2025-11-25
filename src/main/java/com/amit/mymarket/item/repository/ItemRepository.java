package com.amit.mymarket.item.repository;

import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.repository.projection.ItemWithQuantity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

    @Query(value = """
            select items.id          as id,
                   items.title       as title,
                   items.description as description,
                   items.img_path    as imagePath,
                   items.price_minor as priceMinor,
                   coalesce(carts_items.quantity, 0) as quantity
            from shop.items
            left join shop.carts on carts.session_id = :sessionId and carts.status = 'ACTIVE'
            left join shop.carts_items on carts_items.cart_id = carts.id and carts_items.item_id = items.id
            where (:search is null
                   or items.title ilike concat('%', :search, '%')
                   or items.description ilike concat('%', :search, '%'))
            order by
              case when :sort = 'ALPHA' then lower(items.title) end asc nulls last,
              case when :sort = 'PRICE' then items.price_minor end asc nulls last,
              items.id asc
            limit :limit
            offset :offset
            """)
    Flux<ItemWithQuantity> searchItemsWithQuantity(
            String sessionId,
            String search,
            String sort,
            long limit,
            long offset
    );

    @Query(value = """
            select items.id          as id,
                   items.title       as title,
                   items.description as description,
                   items.img_path    as imagePath,
                   items.price_minor as priceMinor,
                   coalesce(carts_items.quantity, 0) as quantity
            from shop.items
            left join shop.carts on carts.session_id = :sessionId and carts.status = 'ACTIVE'
            left join shop.carts_items on carts_items.cart_id = carts.id and carts_items.item_id = items.id
            where items.id = :itemId
            """)
    Mono<ItemWithQuantity> findItemWithQuantity(long itemId, String sessionId);

    @Query(value = """
            select count(*)
            from shop.items
            where (:search is null
                   or items.title ilike concat('%', :search, '%')
                   or items.description ilike concat('%', :search, '%'))
            """)
    Mono<Long> countItemsBySearchQuery(String search);

}
