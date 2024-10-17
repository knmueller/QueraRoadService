CREATE TABLE IF NOT EXISTS sign (
    id SERIAL PRIMARY KEY,
    road_id INT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    foreign key (road_id) references road(id)
);
