CREATE TABLE IF NOT EXISTS endpoint_log (
    log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app_name varchar(50) NOT NULL,
    request_uri varchar(2048) NOT NULL,
    user_ip varchar(45) NOT NULL,
    creation_date  TIMESTAMP WITHOUT TIME ZONE
);