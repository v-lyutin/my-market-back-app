-- liquibase formatted sql

-- changeset v-lyutin:create-items-table
CREATE TABLE ${schemaName}.items (
  id           BIGSERIAL  PRIMARY KEY,
  title        VARCHAR(255)  NOT NULL,
  description  VARCHAR(2048) NOT NULL,
  img_path     VARCHAR(512)          ,
  price_minor  BIGINT        NOT NULL
);

ALTER TABLE ${schemaName}.items
  ADD CONSTRAINT chk_item_price_nonneg CHECK (price_minor >= 0);

-- rollback DROP TABLE ${schemaName}.items;
