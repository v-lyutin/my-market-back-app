-- liquibase formatted sql

-- changeset v-lyutin:create-carts-table
CREATE TABLE ${schemaName}.carts (
  id          BIGSERIAL PRIMARY KEY,
  session_id  VARCHAR(128) NULL,
  status      VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- rollback DROP TABLE ${schemaName}.carts;
