-- liquibase formatted sql

-- changeset v-lyutin:create-roles-table
CREATE TABLE ${schemaName}.roles (
  id    BIGSERIAL PRIMARY KEY,
  name  VARCHAR(64) NOT NULL,
  CONSTRAINT uq_roles_name UNIQUE (name)
);
-- rollback DROP TABLE ${schemaName}.roles;

-- changeset v-lyutin:seed-roles
INSERT INTO ${schemaName}.roles (name) VALUES ('ROLE_USER');
INSERT INTO ${schemaName}.roles (name) VALUES ('ROLE_MANAGER');
-- rollback DELETE FROM ${schemaName}.roles WHERE name IN ('ROLE_USER', 'ROLE_MANAGER');

-- changeset v-lyutin:create-users-table
CREATE TABLE ${schemaName}.users (
  id            BIGSERIAL PRIMARY KEY,
  username      VARCHAR(128) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled       BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_users_username UNIQUE (username)
);
-- rollback DROP TABLE ${schemaName}.users;

-- changeset v-lyutin:create-users-roles-table
CREATE TABLE ${schemaName}.users_roles (
  id      BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES ${schemaName}.users(id) ON DELETE CASCADE,
  CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES ${schemaName}.roles(id) ON DELETE CASCADE,
  CONSTRAINT uq_users_roles_user_role UNIQUE (user_id, role_id)
);
-- rollback DROP TABLE ${schemaName}.users_roles;
