CREATE TABLE device (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        brand VARCHAR(255) NOT NULL,
                        state VARCHAR(20) NOT NULL,
                        creation_time TIMESTAMP NOT NULL
);