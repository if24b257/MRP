CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username TEXT UNIQUE NOT NULL,
                       password_hash TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE media (
                       id SERIAL PRIMARY KEY,
                       creator_id INT REFERENCES users(id),
                       title TEXT NOT NULL,
                       description TEXT,
                       media_type TEXT NOT NULL,
                       release_year INT,
                       created_at TIMESTAMP DEFAULT now()
);
