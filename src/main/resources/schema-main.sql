CREATE TABLE IF NOT EXISTS task (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      title VARCHAR(255),
                      description VARCHAR(255),
                      status VARCHAR(50),
                      assignee_id BIGINT,
                      created_at TIMESTAMP,
                      updated_at TIMESTAMP,
                      started_at TIMESTAMP,
                      time_spent BIGINT
);