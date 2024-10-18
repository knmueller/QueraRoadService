CREATE TABLE IF NOT EXISTS sign (
    id SERIAL PRIMARY KEY,
    road_id INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    foreign key (road_id) references road(id)
);
