-- liquibase formatted sql

-- changeset v-lyutin:seed-users
INSERT INTO ${schemaName}.users (username, password_hash, enabled)
VALUES
  ('user',  '$2a$10$6uGhvdf/SOCxZjJXRolrbeoDkM5BrpqkpxYCKEg/h.YSGz6Ji/2uO',  TRUE),
  ('manager', '$2a$10$6uGhvdf/SOCxZjJXRolrbeoDkM5BrpqkpxYCKEg/h.YSGz6Ji/2uO', TRUE);
-- rollback DELETE FROM ${schemaName}.users WHERE username IN ('user','manager');

-- changeset v-lyutin:seed-users-roles
INSERT INTO ${schemaName}.users_roles (user_id, role_id)
SELECT u.id, r.id
FROM ${schemaName}.users u
JOIN ${schemaName}.roles r ON r.name = 'ROLE_USER'
WHERE u.username = 'user';

INSERT INTO ${schemaName}.users_roles (user_id, role_id)
SELECT u.id, r.id
FROM ${schemaName}.users u
JOIN ${schemaName}.roles r ON r.name IN ('ROLE_MANAGER','ROLE_USER')
WHERE u.username = 'manager';
-- rollback DELETE FROM ${schemaName}.users_roles ur USING ${schemaName}.users u WHERE ur.user_id = u.id AND u.username IN ('user','manager');