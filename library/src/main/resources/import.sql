INSERT INTO tb_role (authority) VALUES ('ROLE_ADMIN');
INSERT INTO tb_role (authority) VALUES ('ROLE_USER');

INSERT INTO tb_user (name, email, password) VALUES ('Admin', 'felipegabrilldev@gmail.com', '$2a$10$vNKNok3/WsCAaxF6ncmAX.GoGlwoliEkT/Hx/XQFJmxFl9hvCEYy2');
INSERT INTO tb_user (name, email, password) VALUES ('Felipe Gabriel', 'felipe@biblioteca.com', '$2a$10$vNKNok3/WsCAaxF6ncmAX.GoGlwoliEkT/Hx/XQFJmxFl9hvCEYy2');

INSERT INTO tb_user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO tb_user_role (user_id, role_id) VALUES (2, 2);