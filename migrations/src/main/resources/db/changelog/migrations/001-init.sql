--liquibase formatted sql

--changeset autentigo:init
CREATE SCHEMA IF NOT EXISTS autentigo;

CREATE TABLE IF NOT EXISTS autentigo.users (
    id           UUID NOT NULL,
    email        TEXT NOT NULL,
    password     TEXT,
    totp_secret  TEXT NOT NULL,
    CONSTRAINT users_unique_id PRIMARY KEY (id),
    CONSTRAINT users_unique_email UNIQUE (email)
);

--rollback DROP TABLE IF EXISTS autentigo.users;
--rollback DROP SCHEMA IF EXISTS autentigo;
