CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(20) NULL,
    email VARCHAR(255) NOT NULL,
    desired_job VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    last_login_at DATETIME NULL,
    CONSTRAINT uk_users_login_id UNIQUE (login_id),
    CONSTRAINT uk_users_email UNIQUE (email)
);
