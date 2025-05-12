INSERT INTO user (username, password, email, provider)
VALUES ('testuser', 'testpassword', 'test@test.com', 'local');

INSERT INTO todo (title, description, completed, user_id)
VALUES ('First Todo', 'This is a sample task', 0, 1);

INSERT INTO todo (title, description, completed, user_id)
VALUES ('Second Todo', 'Another sample task', 1, 1);