-- liquibase formatted sql

-- changeset v-lyutin:create-orders-table
CREATE TABLE ${schemaName}.orders (
  id           BIGSERIAL PRIMARY KEY,
  session_id   VARCHAR(128) NULL,
  status       VARCHAR(16) NOT NULL DEFAULT 'CREATED',
  total_minor  BIGINT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_orders_total_nonneg CHECK (total_minor >= 0)
);

-- rollback DROP TABLE ${schemaName}.orders;
