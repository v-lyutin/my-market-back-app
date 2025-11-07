-- liquibase formatted sql

-- changeset v-lyutin:create-carts-items-table
CREATE TABLE ${schemaName}.carts_items (
  cart_id    BIGINT NOT NULL,
  item_id    BIGINT NOT NULL,
  quantity   INTEGER NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT pk_carts_items PRIMARY KEY (cart_id, item_id),
  CONSTRAINT fk_carts_items_carts FOREIGN KEY (cart_id) REFERENCES ${schemaName}.carts(id) ON DELETE CASCADE,
  CONSTRAINT fk_carts_items_items FOREIGN KEY (item_id) REFERENCES ${schemaName}.items(id),
  CONSTRAINT chk_carts_items_quantity_pos CHECK (quantity > 0)
);

-- rollback DROP TABLE ${schemaName}.carts_items;
