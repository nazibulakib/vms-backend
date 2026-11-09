# Authentication API

The API base path is `/api/v1`. Authentication uses an environment-configured in-memory course account and short-lived opaque Bearer tokens. Passwords are checked by Spring Security using a password encoder. Tokens are held in server memory as hashes and are invalidated on logout or backend restart.

## Login

`POST /auth/login` is public and accepts:

```json
{
  "email": "vms.admin@starkgroup.local",
  "password": "<local VMS_AUTH_PASSWORD>"
}
```

Successful response (`200 OK`):

```json
{
  "accessToken": "<opaque-token>",
  "tokenType": "Bearer",
  "expiresInSeconds": 28800,
  "email": "vms.admin@starkgroup.local"
}
```

Invalid credentials return `401 Unauthorized`:

```json
{ "message": "Invalid email or password." }
```

Malformed input returns `400 Bad Request` with a client-safe `message`.

## Authenticated requests

All vehicle endpoints and `/auth/session` require:

```http
Authorization: Bearer <accessToken>
```

Missing, expired, or revoked tokens return `401 Unauthorized` with `{ "message": "Authentication is required." }`.

`GET /auth/session` returns the authenticated email. `POST /auth/logout` revokes the current token and returns `204 No Content`.

## Local credentials

Set `VMS_AUTH_USERNAME`, `VMS_AUTH_PASSWORD`, and optionally `VMS_AUTH_TOKEN_TTL_SECONDS` in the ignored backend `.env` file or the deployment environment. Never commit the real password. The current local course account uses the username in `.env`; the password is also kept there.
