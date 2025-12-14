-- liquibase formatted sql

-- changeset v-lyutin:create-carts-items-table
CREATE TABLE ${schemaName}.carts_items (
  id         BIGSERIAL PRIMARY KEY,
  cart_id    BIGINT NOT NULL,
  item_id    BIGINT NOT NULL,
  quantity   INTEGER NOT NULL,
  CONSTRAINT fk_carts_items_carts FOREIGN KEY (cart_id) REFERENCES ${schemaName}.carts(id) ON DELETE CASCADE,
  CONSTRAINT fk_carts_items_items FOREIGN KEY (item_id) REFERENCES ${schemaName}.items(id),
  CONSTRAINT uq_carts_items_cart_item UNIQUE (cart_id, item_id),
  CONSTRAINT chk_carts_items_quantity_pos CHECK (quantity > 0)
);

-- rollback DROP TABLE ${schemaName}.carts_items;
