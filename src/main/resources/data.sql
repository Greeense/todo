INSERT INTO user (username, password, email, provider)
VALUES ('testuser', 'testpassword', 'test@test.com', 'local');

INSERT INTO todo (content, completed, user_id)
VALUES ('finish homework until Friday', 0, 1);
