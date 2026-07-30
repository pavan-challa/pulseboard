CREATE TABLE IF NOT EXISTS endpoints (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  url VARCHAR(500),
  check_interval_seconds INT DEFAULT 60,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS check_results (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  endpoint_id BIGINT,
  status VARCHAR(10),
  response_time_ms BIGINT,
  checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (endpoint_id) REFERENCES endpoints(id)
);

CREATE TABLE IF NOT EXISTS incidents (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  endpoint_id BIGINT,
  opened_at TIMESTAMP,
  resolved_at TIMESTAMP,
  status VARCHAR(20) DEFAULT 'OPEN',
  FOREIGN KEY (endpoint_id) REFERENCES endpoints(id)
);
