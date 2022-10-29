CREATE TABLE IF NOT EXISTS category (
    category_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_name      VARCHAR(100),
    last_update        TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (category_name)
);

CREATE TABLE IF NOT EXISTS users (
    user_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_name   varchar(200) NOT NULL,
    email       varchar(254) NOT NULL,
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS events (
    event_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title                VARCHAR(120) NOT NULL,
    creation_date        TIMESTAMP  WITHOUT TIME ZONE NOT NULL,
    publish_date         TIMESTAMP  WITHOUT TIME ZONE,
    event_date           TIMESTAMP  WITHOUT TIME ZONE NOT NULL,
    annotation           VARCHAR(2000) NOT NULL,
    category_id          BIGINT NOT NULL,
    description          VARCHAR(7000) NOT NULL,
    latitude             REAL NULL,
    longitude            REAL NULL,
    paid                 BOOLEAN NOT NULL DEFAULT FALSE,
    participant_limit    INT DEFAULT 0,
    request_moderation   BOOLEAN DEFAULT TRUE,
    state                VARCHAR(20) NOT NULL,
    initiator_id         BIGINT NOT NULL,
    CONSTRAINT fk_events_to_users
        FOREIGN KEY (initiator_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_category_to_users
        FOREIGN KEY (category_id)
            REFERENCES category(category_id)
            ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS requests (
    request_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id        BIGINT NOT NULL,
    requester_id    BIGINT NOT NULL,
    status          VARCHAR(20),
    creation_date   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_requests_to_users
        FOREIGN KEY (requester_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_requests_to_events
        FOREIGN KEY (event_id)
            REFERENCES events(event_id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS compilations (
    compilation_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title             VARCHAR(120) NOT NULL,
    pinned            BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE IF NOT EXISTS compilations_events (
    compilation_id    BIGINT,
    event_id          BIGINT,
    CONSTRAINT UQ_COMP_ID_EVENT_ID UNIQUE(compilation_id, event_id),
    CONSTRAINT fk_compilations_events_to_compilations
        FOREIGN KEY (compilation_id)
            REFERENCES compilations(compilation_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_compilations_events_to_events
        FOREIGN KEY (event_id)
            REFERENCES events(event_id)
            ON DELETE CASCADE
);
