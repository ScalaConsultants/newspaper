CREATE TABLE Updated_Content (
    id INT NOT NULL PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    time_added TIMESTAMP NOT NULL,
    was_sent BOOLEAN NOT NULL
);