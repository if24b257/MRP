-- Legt die Tabellen für Benutzer und Medien an, inklusive Minimalfeldern.
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS media (
    id SERIAL PRIMARY KEY,
    created_by_user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    media_type TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ratings (
    id SERIAL PRIMARY KEY,
    media_id INT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    star_value INT NOT NULL CHECK (star_value BETWEEN 1 AND 5),
    comment TEXT,
    comment_confirmed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now(),
    CONSTRAINT unique_rating_per_user UNIQUE (media_id, user_id)
);

CREATE TABLE IF NOT EXISTS rating_likes (
    rating_id INT NOT NULL REFERENCES ratings(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    liked_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (rating_id, user_id)
);
