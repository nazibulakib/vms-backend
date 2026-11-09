# Vehicle API

The API is served under `/api/v1`. Vehicle JSON uses these fields: `id` (response only), `vehicleNumber`, `company`, `type`, `brand`, `model`, and `status`. All request fields are required non-blank strings. Maximum lengths are 64 for `vehicleNumber`, `type`, and `status`; 120 for `company`; and 100 for `brand` and `model`.

All vehicle endpoints require the `Authorization: Bearer <accessToken>` header returned by `POST /auth/login`. See [auth-api.md](auth-api.md) for the login contract.

Vehicle numbers are trimmed and converted to uppercase before storage and comparison. They must be unique.

## Endpoints

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `POST` | `/vehicles` | Create a vehicle | `201 Created`, response body and `Location` header |
| `GET` | `/vehicles` | List vehicles ordered by vehicle number | `200 OK`, JSON array |
| `GET` | `/vehicles/{id}` | Read one vehicle | `200 OK`, response body |
| `PUT` | `/vehicles/{id}` | Replace the vehicle fields | `200 OK`, updated response body |
| `DELETE` | `/vehicles/{id}` | Delete one vehicle | `204 No Content` |

## Request body

`POST /vehicles` and `PUT /vehicles/{id}` accept the same JSON shape:

```json
{
  "vehicleNumber": "ABC-123",
  "company": "Stark Group",
  "type": "Truck",
  "brand": "Volvo",
  "model": "FH",
  "status": "Active"
}
```

## Response body

```json
{
  "id": 1,
  "vehicleNumber": "ABC-123",
  "company": "Stark Group",
  "type": "Truck",
  "brand": "Volvo",
  "model": "FH",
  "status": "Active"
}
```

## Common errors

Errors use `{ "message": "..." }`.

| Status | Cause | Example message |
| --- | --- | --- |
| `400 Bad Request` | Missing, blank, or overlong request field | `Vehicle number is required` |
| `404 Not Found` | Vehicle ID does not exist | `Vehicle with id 42 was not found.` |
| `409 Conflict` | Vehicle number already exists | `A vehicle with this vehicle number already exists.` |
