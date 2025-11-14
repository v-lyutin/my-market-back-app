package com.amit.mymarket.item.repository;

import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.repository.projection.ItemWithCountRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(value = """
            select items.id as id,
                   items.title as title,
                   items.description as description,
                   items.img_path as imagePath,
                   items.price_minor as priceMinor,
                   coalesce(carts_items.quantity, 0) as count
            from shop.items
            left join shop.carts on carts.session_id = :sessionId and carts.status = 'ACTIVE'
            left join shop.carts_items on carts_items.cart_id = carts.id and carts_items.item_id = items.id
            where (:search is null
                   or items.title ilike concat('%%', :search, '%%')
                   or items.description ilike concat('%%', :search, '%%'))
            order by
              case when :sort = 'ALPHA' then lower(items.title) end asc nulls last,
              case when :sort = 'PRICE' then items.price_minor end asc nulls last,
              items.id asc
            """,
            countQuery = """
                    select count(*)
                    from shop.items
                    where (:search is null
                           or items.title ilike concat('%%', :search, '%%')
                           or items.description ilike concat('%%', :search, '%%'))
                    """,
            nativeQuery = true
    )
    Page<ItemWithCountRow> findCatalogWithCounts(
            @Param(value = "sessionId") String sessionId,
            @Param(value = "search") String search,
            @Param(value = "sort") String sort,
            Pageable pageable
    );

    @Query(value = """
            select items.id as id,
                   items.title as title,
                   items.description as description,
                   items.img_path as imagePath,
                   items.price_minor as priceMinor,
                   coalesce(carts_items.quantity, 0) as count
            from shop.items
            left join shop.carts on carts.session_id = :sessionId and carts.status = 'ACTIVE'
            left join shop.carts_items on carts_items.cart_id = carts.id and carts_items.item_id = items.id
            where items.id = :itemId
            """,
            nativeQuery = true)
    ItemWithCountRow findItemWithCount(@Param(value = "itemId") long itemId, @Param(value = "sessionId") String sessionId);

}
