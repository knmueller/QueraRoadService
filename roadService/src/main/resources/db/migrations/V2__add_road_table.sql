CREATE TABLE IF NOT EXISTS road (
    id SERIAL PRIMARY KEY,
    surface_type VARCHAR NOT NULL,
    intersection_id INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    foreign key (intersection_id) references intersection(id)
);

