# autentigo

Standalone user authentication microservice.

## About

Provides user registration, basic authentication, and JWT access/ID token issuance
and validation over an HTTP API, backed by PostgreSQL.

## Deployment

To run the service locally:

```shell
cp .env.example .env  # fill in secrets
just up
```

## Configuration

Environment variables (see `src/main/resources/reference.conf`):

| Variable                     | Description                     |
|------------------------------|---------------------------------|
| `APP_PORT`                   | Port the service listens on.    |
| `POSTGRES_URL`               | JDBC URL for PostgreSQL.        |
| `POSTGRES_USER`              | PostgreSQL user.                |
| `POSTGRES_PASSWORD`          | PostgreSQL password.            |
| `JWT_ISSUER`                 | Value used in JWT `iss` claims. |
| `JWT_SECRET_KEY`             | Secret key used to sign JWTs.   |