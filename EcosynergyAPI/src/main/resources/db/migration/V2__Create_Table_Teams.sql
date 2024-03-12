CREATE TABLE teams (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(255) UNIQUE NOT NULL,
                                     description TEXT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);