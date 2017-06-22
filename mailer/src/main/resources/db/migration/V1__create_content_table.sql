CREATE TABLE send_orders (
    id SERIAL NOT NULL PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    for_user VARCHAR(100) NOT NULL,
    time_added TIMESTAMP NOT NULL,
    was_sent BOOLEAN NOT NULL
);