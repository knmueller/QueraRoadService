CREATE TABLE IF NOT EXISTS intersection (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
