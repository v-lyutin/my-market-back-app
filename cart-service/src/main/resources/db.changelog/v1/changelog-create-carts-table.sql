-- liquibase formatted sql

-- changeset v-lyutin:create-carts-table
CREATE TABLE ${schemaName}.carts (
  id          BIGSERIAL PRIMARY KEY,
  user_id     VARCHAR(128) NOT NULL,
  status      VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
);

-- rollback DROP TABLE ${schemaName}.carts;
