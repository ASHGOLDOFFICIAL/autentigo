# autentigo

Standalone user authentication microservice.

## About

Provides user registration, basic authentication, JWT access/ID token issuance and validation, and email-based password
reset over an HTTP API, backed by PostgreSQL.

## Deployment

To run the service locally:

```shell
cp .env.example .env  # fill in secrets
just up
```

## Configuration

Environment variables (see `src/main/resources/reference.conf`):

| Variable            | Description                                          |
|---------------------|------------------------------------------------------|
| `APP_PORT`          | Port the service listens on.                         |
| `POSTGRES_URL`      | JDBC URL for PostgreSQL.                             |
| `POSTGRES_USER`     | PostgreSQL user.                                     |
| `POSTGRES_PASSWORD` | PostgreSQL password.                                 |
| `JWT_ISSUER`        | Value used in JWT `iss` claims.                      |
| `JWT_SECRET_KEY`    | Secret key used to sign JWTs.                        |
| `SMTP_HOST`         | SMTP server host for outgoing password-reset emails. |
| `SMTP_PORT`         | SMTP server port.                                    |
| `SMTP_USERNAME`     | SMTP auth username.                                  |
| `SMTP_PASSWORD`     | SMTP auth password.                                  |
| `SMTP_FROM_ADDRESS` | Address password-reset emails are sent from.         |