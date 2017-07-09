CREATE TABLE user_subscriptions (
    user_email VARCHAR(100) NOT NULL PRIMARY KEY,
    user_name VARCHAR(100),
    time_added TIMESTAMP NOT NULL
);