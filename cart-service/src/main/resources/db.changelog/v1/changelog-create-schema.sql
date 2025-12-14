-- liquibase formatted sql

-- changeset v-lyutin:create-schema
CREATE SCHEMA IF NOT EXISTS ${schemaName};

-- rollback DROP SCHEMA IF EXISTS ${schemaName} CASCADE;