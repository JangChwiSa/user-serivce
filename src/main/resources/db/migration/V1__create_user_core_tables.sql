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

CREATE TABLE user_disabilities (
    disability_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    disability_type VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_user_disabilities_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uk_user_disability UNIQUE (user_id, disability_type)
);
