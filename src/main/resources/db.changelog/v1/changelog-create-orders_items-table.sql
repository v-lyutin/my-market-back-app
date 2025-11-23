-- liquibase formatted sql

-- changeset v-lyutin:create-orders-items-table
CREATE TABLE ${schemaName}.orders_items (
  id                    BIGSERIAL PRIMARY KEY,
  order_id              BIGINT NOT NULL,
  item_id               BIGINT NOT NULL,
  title_snapshot        VARCHAR(255) NOT NULL,
  price_minor_snapshot  BIGINT NOT NULL,
  quantity              INTEGER NOT NULL,
  CONSTRAINT fk_orders_items_orders FOREIGN KEY (order_id) REFERENCES ${schemaName}.orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_orders_items_items FOREIGN KEY (item_id) REFERENCES ${schemaName}.items(id),
  CONSTRAINT uq_carts_items_cart_item UNIQUE (order_id, item_id)
  CONSTRAINT chk_orders_items_price_nonneg CHECK (price_minor_snapshot >= 0),
  CONSTRAINT chk_orders_items_quantity_pos CHECK (quantity > 0)
);

-- rollback DROP TABLE ${schemaName}.orders_items;
