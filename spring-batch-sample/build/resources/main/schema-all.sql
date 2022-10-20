-- DROP TABLE people IF EXISTS;

CREATE TABLE IF NOT EXISTS people(
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    line_no INT,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);
