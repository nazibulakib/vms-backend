# VMS Backend

Standalone Java Spring Boot service for the Vehicle Management System. The Flutter Web application remains in the separate `vms-frontend` repository; it is not included in this Maven build.

## Current scope

The backend includes Spring MVC, Spring Security, Spring Data JPA, Bean Validation, MySQL persistence, JSON authentication, and the vehicle REST API. It remains a separate project from the Flutter frontend.

## Authentication

`POST /api/v1/auth/login` accepts an email and password and returns an 8-hour opaque Bearer token. Vehicle operations require that token. `GET /api/v1/auth/session` validates a saved token, and `POST /api/v1/auth/logout` revokes it. Read the [authentication contract](docs/auth-api.md).

The course user is configured through `VMS_AUTH_USERNAME` and `VMS_AUTH_PASSWORD` in `.env` or deployment environment variables. Spring Security applies its password encoder to the in-memory course account at startup. Tokens are held server-side as hashes and are revoked on restart. Passwords and tokens are not logged.

## Local MySQL connection

The local connection defaults to MySQL at `127.0.0.1:3306`, database `vms_db`, and application user `vms_app`. Create a private `.env` from `.env.example` and set strong local database and course-account passwords. `.env` is ignored by Git; never commit its contents or administrator credentials.

The application expects the `vehicles` table to match `VehicleEntity`; the local developer database has this table created with a unique constraint on `vehicle_number`. Hibernate schema generation remains disabled (`ddl-auto: none`).

The endpoint `GET /api/v1/health/database` executes `SELECT 1` and returns HTTP 200 when the configured database is reachable, or HTTP 503 when the connectivity check fails.

H2 is included only for the test profile; tests do not require the MySQL service to be running. Test login credentials are generated at runtime.

## Requirements

- Java 17 or newer
- Apache Maven 3.6.3 or newer

## Run and verify

```bash
mvn clean verify
mvn spring-boot:run
curl --fail http://localhost:8080/api/v1/health/database
```

The service listens on port `8080` with the `/api/v1` context path by default. Override these with `SERVER_PORT` and `SERVER_SERVLET_CONTEXT_PATH`.

The JDBC URL and credentials can be overridden with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Never commit production credentials.

## Package responsibilities

| Package | Responsibility |
| --- | --- |
| `api.controller` | HTTP request and response adapters |
| `api.dto` | Request and response payload types |
| `application.service` | Use-case orchestration and transaction boundaries |
| `domain.model` | Business concepts and rules |
| `infrastructure.persistence.entity` | JPA persistence mappings |
| `infrastructure.persistence.repository` | Database access adapters |
| `shared.validation` | Reusable validation support |
| `shared.exception` | Error types and HTTP error mapping |

Keep transport types separate from persistence entities. Controllers should delegate to services; repositories should remain behind application/domain-facing interfaces as the code grows.
