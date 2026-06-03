INSERT INTO roles (name)
VALUES ('ROLE_USER');

INSERT INTO roles (name)
VALUES ('ROLE_ADMIN');

INSERT INTO app_users (username, password)
VALUES ('admin', '$2a$10$Ve7qOdv9aLswXojB87Jw6eu3Qyz393oCJVWrO4MQltwssf1r7K4O2');

INSERT INTO user_role_mappings (user_id, role_id)
SELECT u.id, r.id
FROM app_users u
         JOIN roles r ON r.name = 'ROLE_ADMIN' AND u.username = 'admin';